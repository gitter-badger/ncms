package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.web.GenericResponseWrapper;
import com.softmotions.weboot.lifecycle.Dispose;
import com.softmotions.weboot.lifecycle.Start;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.map.Flat3Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Singleton
public class AsmWebRefAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmWebRefAM.class);

    public static final String[] TYPES = new String[]{"webref"};

    CloseableHttpClient httpclient;

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
        return attr;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {

        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || StringUtils.isBlank(attr.getEffectiveValue())) {
            return null;
        }
        AsmOptions opts = new AsmOptions();
        if (attr.getOptions() != null) {
            opts.loadOptions(attr.getOptions());
        }
        String location = attr.getEffectiveValue();
        URI uri;
        try {
            uri = new URI(location);
        } catch (URISyntaxException e) {
            log.warn("Invalid resource location: " + location +
                     " asm: " + ctx.getAsm() + " attribute: " + attrname +
                     " error: " + e.getMessage());
            return null;
        }
        if (BooleanUtils.toBoolean(opts.getString("asLocation"))) {
            return location;
        }
        if (log.isDebugEnabled()) {
            log.debug("Including resource: '" + uri + '\'');
        }
        Object res;
        if (uri.getScheme() == null) {
            res = internalInclude(ctx, attrname, uri, options);
        } else {
            res = externalInclude(ctx, attrname, uri, options);
        }
        //ctx.setNextEscapeSkipping(!BooleanUtils.toBoolean(opts.getString("escape")));
        return res;
    }

    private String externalInclude(AsmRendererContext ctx, String attrname,
                                   URI location, Map<String, String> options) {
        StringWriter out = new StringWriter(1024);
        String cs = ctx.getServletRequest().getCharacterEncoding();
        if (cs == null) {
            cs = "UTF-8";
        }
        try {
            if (location.getScheme().startsWith("http")) {
                //HTTP GET
                if (options != null && !options.isEmpty()) {
                    URIBuilder ub = new URIBuilder(location);
                    for (Map.Entry<String, String> opt : options.entrySet()) {
                        ub.addParameter(opt.getKey(), opt.getValue());
                    }
                    location = ub.build();
                }
                HttpGet httpGet = new HttpGet(location);
                CloseableHttpResponse hresp = null;
                InputStream is = null;
                try {
                    httpclient = HttpClients.createSystem();
                    hresp = httpclient.execute(httpGet);
                    if (hresp.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                        log.warn("Invalid resource response status code: " + hresp.getStatusLine().getStatusCode() +
                                 " location: " + location +
                                 " asm: " + ctx.getAsm() +
                                 " attribute: " + attrname +
                                 " response: " + out.toString());
                        return null;
                    }
                    is = hresp.getEntity().getContent();
                    IOUtils.copy(is, out, cs);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }
                    if (hresp != null) {
                        try {
                            hresp.close();
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }
                    httpGet.reset();
                }
            } else {
                URL url = location.toURL();
                try (InputStream is = url.openStream()) {
                    IOUtils.copy(is, out, cs);
                }
            }
        } catch (Exception e) {
            log.warn("Unable to load resource: " + location +
                     " asm: " + ctx.getAsm() +
                     " attribute: " + attrname, e);
            return null;
        }
        return out.toString();
    }

    private String internalInclude(AsmRendererContext ctx, String attrname,
                                   URI location, Map<String, String> options) {
        String cs = ctx.getServletRequest().getCharacterEncoding();
        if (cs == null) {
            cs = "UTF-8";
        }
        List<NameValuePair> qparams = URLEncodedUtils.parse(location, cs);
        if (!qparams.isEmpty()) {
            if (options == null) {
                options = new Flat3Map();
            }
            for (NameValuePair pair : qparams) {
                if (!options.containsKey(pair.getName())) {
                    options.put(pair.getName(), pair.getValue());
                }
            }
        }
        StringWriter out = new StringWriter(1024);
        HttpServletResponse resp = new GenericResponseWrapper(ctx.getServletResponse(), out, false);
        HttpServletRequest req = new InternalHttpRequest(ctx.getServletRequest(), options);
        RequestDispatcher rd = ctx.getServletRequest().getRequestDispatcher(location.getPath());
        try {
            rd.include(req, resp);
        } catch (IOException | ServletException e) {
            log.warn("Failed to include resource: " + location +
                     " asm: " + ctx.getAsm() +
                     " attribute: " + attrname, e);
            return null;
        }
        if (resp.getStatus() == HttpServletResponse.SC_OK) {
            return out.toString();
        } else {
            log.warn("Invalid resource response status code: " + resp.getStatus() +
                     " location: " + location +
                     " asm: " + ctx.getAsm() +
                     " attribute: " + attrname +
                     " response: " + out.toString());
            return null;
        }
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions opts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, opts,
                                        "asLocation");
        attr.setOptions(opts.toString());
        attr.setEffectiveValue(val.has("value") ? val.get("value").asText() : null);
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val.hasNonNull("value") ? val.get("value").asText().trim() : null);
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {

    }


    @Start(order = 10)
    public void start() {
        httpclient = HttpClients.createSystem();
    }

    @Dispose(order = 10)
    public void stop() {
        if (httpclient != null) {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("", e);
            }
            httpclient = null;
        }
    }


    @SuppressWarnings("unchecked")
    static final class InternalHttpRequest extends HttpServletRequestWrapper {

        final Map<String, String> params;

        Map<String, String[]> paramsArr;

        InternalHttpRequest(HttpServletRequest request, Map<String, String> params) {
            super(request);
            this.params = (params != null ? params : Collections.EMPTY_MAP);

        }

        public String getParameter(String name) {
            return params.get(name);
        }

        public Map<String, String[]> getParameterMap() {
            if (paramsArr != null) {
                return paramsArr;
            }
            for (final Map.Entry<String, String> e : params.entrySet()) {
                paramsArr.put(e.getKey(), new String[]{e.getValue()});
            }
            return paramsArr;
        }

        public Enumeration<String> getParameterNames() {
            return IteratorUtils.asEnumeration(params.keySet().iterator());
        }

        public String[] getParameterValues(String name) {
            String pv = params.get(name);
            return pv != null ? new String[]{pv} : null;
        }
    }
}
