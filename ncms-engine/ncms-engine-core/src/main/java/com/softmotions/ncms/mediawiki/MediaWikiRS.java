package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.filter.Encoder;
import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.html.Redirect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Media-wiki services.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("mw")
public class MediaWikiRS {

    // 100px-/121/P4033297.JPG
    // /123/bg-interview.png
    private static final Pattern RES_REGEXP
            = Pattern.compile("((\\d+)px\\-)?/(\\d+)/.*");

    // Image:100px-/121/P4033297.JPG
    // File:/121/P4033297.JPG
    private static final Pattern LINK_FILE_REGEXP
            = Pattern.compile("(Image|File|Media):((\\d+)px\\-)?/?(\\d+)/?.*");

    private static final Pattern EXT_LINK_REGEXP
            = Pattern.compile("((Http|Https|Ftp|Smb|Sftp|Scp)://)(.*)");

    private static final Pattern CHECK_ENCODED_REGEXP
            = Pattern.compile(".*[^a-zA-Z0-9-_.!~*'()/#%]+.*");

    private final MediaRepository repository;

    private final NcmsMessages messages;

    private final NcmsEnvironment env;

    @Inject
    public MediaWikiRS(MediaRepository repository,
                       NcmsMessages messages,
                       NcmsEnvironment env) {
        this.repository = repository;
        this.messages = messages;
        this.env = env;
    }

    @GET
    @Path("res/{spec:.*}")
    public Response res(@PathParam("spec") String spec,
                        @Context HttpServletRequest req) throws Exception {
        Matcher matcher = RES_REGEXP.matcher(spec);
        if (!matcher.matches()) {
            //todo fallback for old site format
            throw new BadRequestException("");
        }
        Integer width = null;
        String widthStr = matcher.group(2);
        Long id = Long.parseLong(matcher.group(3));
        if (widthStr != null) {
            width = Integer.parseInt(widthStr);
        } else {
            int maxWidth = env.xcfg().getInt("mediawiki.max-inline-image-width-px", 0);
            if (maxWidth > 0) {
                MediaResource mres = repository.findMediaResource(id, messages.getLocale(req));
                if (mres == null) {
                    throw new NotFoundException("");
                }
                if (CTypeUtils.isImageContentType(mres.getContentType())) {
                    if (mres.getImageWidth() > maxWidth) { //restrict maximal image width
                        repository.ensureResizedImage(id, maxWidth, null, MediaRepository.RESIZE_SKIP_SMALL);
                        width = maxWidth;
                    }
                }
            }
        }
        return repository.get(id, req, width, null, true);
    }

    @GET
    @Path("link/{spec:(Image|File|Media):.*}")
    public Response link(@PathParam("spec") String spec,
                         @Context HttpServletRequest req) throws Exception {
        Matcher matcher = LINK_FILE_REGEXP.matcher(spec);
        if (!matcher.matches()) {
            //todo fallback for old site format
            throw new BadRequestException("");
        }
        Long id = Long.parseLong(matcher.group(4));
        return repository.get(id, req, null, null, true);
    }

    @GET
    @Path("link/{spec:Page:.*}")
    public Redirect pageLink(@PathParam("spec") String spec,
                             @Context HttpServletRequest req) throws Exception {
        String title = spec.substring("Page:".length());
        String link = env.getServletContext().getContextPath() + env.getNcmsPrefix() + "/" + title;
        return new Redirect(link);
    }

    @GET
    @Path("link/{spec:(Http|Https|Ftp|Smb|Sftp|Scp)://.*}")
    public Redirect externalLink(@PathParam("spec") String spec,
                                 @Context HttpServletRequest req,
                                 @Context HttpServletResponse resp) throws Exception {

        Matcher matcher = EXT_LINK_REGEXP.matcher(spec);
        if (!matcher.matches()) {
            throw new BadRequestException();
        }

        URL url;
        try {
            url = new URL(spec);
        } catch (MalformedURLException ignored) {
            throw new BadRequestException();
        }

        Matcher cematcher = CHECK_ENCODED_REGEXP.matcher(url.getPath());
        String path = cematcher.matches() ? Encoder.encodeUrl(url.getPath()) : url.getPath();

        return new Redirect(new URL(url.getProtocol(), IDN.toASCII(url.getHost()), url.getPort(),
                                    path + (!StringUtils.isBlank(url.getQuery()) ? "?" + url.getQuery() : "")).toURI());
    }
}
