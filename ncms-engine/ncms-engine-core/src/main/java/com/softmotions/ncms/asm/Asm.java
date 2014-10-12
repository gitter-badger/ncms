package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.AbstractIndexedCollection;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.Pair;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.AbstractIterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Assembly.
 * This class is not thread safe for concurrent updating.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@JsonRootName("asm")
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Asm implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Asm.class);

    interface ViewFull {
    }

    interface ViewLarge extends ViewFull {

    }

    @JsonProperty
    Long id;

    @JsonProperty
    String name;

    @JsonProperty
    String hname;

    @JsonProperty
    String type;

    @JsonProperty
    String description;

    @JsonProperty
    AsmCore core;

    @JsonProperty
    String controller;

    @JsonProperty
    String options;

    @JsonProperty
    boolean template;

    @JsonProperty
    boolean published;

    @JsonProperty
    String templateMode;

    Date cdate;

    Date mdate;

    Date edate;

    KVOptions parsedOptions;

    List<Asm> parents;

    AttrsList attributes;

    Long navParentId;

    String navAlias;

    String navCachedPath;

    Collection<String> accessRoles;


    public Asm() {
    }

    public Asm(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Asm(String name) {
        this.name = name;
    }

    public Asm(String name, AsmCore core) {
        this.name = name;
        this.core = core;
    }

    public Asm(String name, AsmCore core, String description, String options) {
        this.name = name;
        this.core = core;
        this.description = description;
        this.options = options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getHname() {
        return hname;
    }

    public void setHname(String hname) {
        this.hname = hname;
    }

    public String getType() {
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = type;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Nullable
    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
        this.parsedOptions = null;
    }

    @Nonnull
    public KVOptions getParsedOptions() {
        String opts = this.options;
        if (opts == null) {
            return new KVOptions();
        }
        this.parsedOptions = new KVOptions(opts);
        return parsedOptions;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    @Nullable
    public String getTemplateMode() {
        return templateMode;
    }

    public void setTemplateMode(String templateMode) {
        this.templateMode = templateMode;
    }

    @Nullable
    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    @Nullable
    public Date getCdate() {
        return cdate;
    }

    public void setCdate(Date cdate) {
        this.cdate = cdate;
    }

    @Nullable
    public Date getEdate() {
        return edate;
    }

    public void setEdate(Date edate) {
        this.edate = edate;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Nullable
    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    @JsonProperty()
    public String getEffectiveController() {
        String c = getController();
        if (c != null || getParents() == null) {
            return c;
        }
        for (final Asm p : getParents()) {
            c = p.getEffectiveController();
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @Nullable
    public AsmCore getCore() {
        return core;
    }

    public void setCore(AsmCore core) {
        this.core = core;
    }

    @Nullable
    @JsonProperty
    public AsmCore getEffectiveCore() {
        AsmCore c = getCore();
        if (c != null || getParents() == null) {
            return c;
        }
        for (final Asm p : getParents()) {
            c = p.getEffectiveCore();
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @JsonView(Asm.ViewFull.class)
    public Collection<String> getAccessRoles() {
        return accessRoles;
    }

    public List<Asm> getParents() {
        return parents;
    }

    public void setParents(List<Asm> parents) {
        this.parents = parents;
    }

    @Nullable
    public Long getNavParentId() {
        return navParentId;
    }

    public void setNavParentId(Long navParentId) {
        this.navParentId = navParentId;
    }

    @Nullable
    public String getNavAlias() {
        return navAlias;
    }

    public void setNavAlias(String navAlias) {
        this.navAlias = navAlias;
    }

    @Nullable
    public String getNavCachedPath() {
        return navCachedPath;
    }

    public void setNavCachedPath(String navCachedPath) {
        this.navCachedPath = navCachedPath;
    }

    public Iterator<Asm> getAllParentsIterator() {
        List<Pair<Asm, Integer>> plist = new ArrayList<>();
        fetchParentsCumulative(plist, 0);
        Collections.sort(plist, (o1, o2) -> {
            int res = Integer.compare(o1.getTwo(), o2.getTwo());
            if (res == 0) {
                Collator coll = Collator.getInstance();
                res = coll.compare(o1.getOne().getName(), o2.getOne().getName());
            }
            return res;
        });
        final Iterator<Pair<Asm, Integer>> pit = plist.iterator();
        return new AbstractIterator<Asm>() {
            protected Asm computeNext() {
                if (pit.hasNext()) {
                    return pit.next().getOne();
                }
                return endOfData();
            }
        };
    }

    private void fetchParentsCumulative(List<Pair<Asm, Integer>> pcont, Integer level) {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return;
        }
        for (final Asm p : getParents()) {
            pcont.add(new Pair<>(p, level));
            p.fetchParentsCumulative(pcont, level + 1);
        }
    }

    public Set<String> getAllParentNames() {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        Set<String> cparents = new HashSet<>();
        for (final Asm p : getParents()) {
            cparents.add(p.getName());
            cparents.addAll(p.getAllParentNames());
        }
        return cparents;

    }

    @JsonProperty()
    public String[] getParentRefs() {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String[] prefs = new String[plist.size()];
        for (int i = 0; i < prefs.length; ++i) {
            Asm p = parents.get(i);
            prefs[i] = p.getId() + ":" + p.getName();
        }
        return prefs;
    }

    @Nullable
    public AsmAttribute getEffectiveAttribute(String name) {
        AsmAttribute attr = getAttribute(name);
        if (attr != null || getParents() == null) {
            return attr;
        }
        for (final Asm p : getParents()) {
            attr = p.getEffectiveAttribute(name);
            if (attr != null) {
                return attr;
            }
        }
        return null;
    }

    public boolean isHasAttribute(String name) {
        return (getEffectiveAttribute(name) != null);
    }

    @Nullable
    public String getEffectiveAttributeAsString(String name, String defVal) {
        AsmAttribute attr = getEffectiveAttribute(name);
        if (attr == null) {
            return defVal;
        }
        return attr.getEffectiveValue();
    }

    public String[] getEffectiveAttributeAsStringArray(String name, ObjectMapper mapper) {
        AsmAttribute attr = getEffectiveAttribute(name);
        if (attr == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String val = attr.getEffectiveValue();
        if (StringUtils.isBlank(val)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        val = val.trim();
        if (val.length() > 1 && val.charAt(0) == '[' && val.charAt(val.length() - 1) == ']') { //todo Naive JSON array detection :(
            try {
                ArrayNode an = (ArrayNode) mapper.readTree(val);
                String[] ret = new String[an.size()];
                for (int i = 0, l = ret.length; i < l; ++i) {
                    ret[i] = an.get(i).asText();
                }
                return ret;
            } catch (Exception ignored) {
                return ArrayUtils.EMPTY_STRING_ARRAY;
            }
        } else {
            return com.softmotions.commons.cont.ArrayUtils.split(val, " ,;");
        }
    }

    @Nullable
    public AsmAttribute getAttribute(String name) {
        return (getAttributes() != null) ? attributes.getIndex().get(name) : null;
    }

    public Collection<AsmAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(AttrsList attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(AsmAttribute attr) {
        if (getAttributes() == null) {
            attributes = new AttrsList();
        }
        attributes.add(attr);
    }

    public void rmAttribute(String name) {
        if (getAttributes() == null || attributes.isEmpty()) {
            return;
        }
        AsmAttribute attr = attributes.getIndex().get(name);
        if (attr != null) {
            attributes.remove(attr);
        }
    }

    public Collection<String> getAttributeNames() {
        List<String> anames = new ArrayList<>(getAttributes().size());
        for (final AsmAttribute a : attributes) {
            anames.add(a.getName());
        }
        return anames;
    }


    public Collection<String> getEffectiveAttributeNames() {
        final Set<String> anames = new HashSet<>(getAttributes().size() * 2);
        if (attributes != null) {
            for (final AsmAttribute a : attributes) {
                anames.add(a.getName());
            }
        }
        if (getParents() != null) {
            for (final Asm p : getParents()) {
                anames.addAll(p.getEffectiveAttributeNames());
            }
        }
        return anames;
    }

    @JsonProperty
    public Collection<AsmAttribute> getEffectiveAttributes() {
        Collection<AsmAttribute> attrs = getAttributes();
        ArrayList<AsmAttribute> res =
                new ArrayList<>(attrs != null && attrs.size() > 10 ?
                                attrs.size() * 2 : 10);
        addSortedChainAttributes(res, this);
        Map<String, Integer> pmap = new HashMap<>();
        for (int i = 0; i < res.size(); ++i) {
            AsmAttribute a = res.get(i);
            Integer pind = pmap.get(a.getName());
            if (pind != null) {
                a.setOverridenParent(res.get(pind));
                res.set(pind, a);
                res.remove(i);
                --i;
            } else {
                pmap.put(a.getName(), i);
            }
        }
        return res;
    }

    private void addSortedChainAttributes(ArrayList<AsmAttribute> res, Asm asm) {
        List<AsmAttribute> slist = asm.getSortedLocalAttributes();
        res.addAll(0, slist);
        if (getParents() != null) {
            for (final Asm p : asm.getParents()) {
                addSortedChainAttributes(res, p);
            }
        }
    }

    private List<AsmAttribute> getSortedLocalAttributes() {
        if (getAttributes() == null) {
            return Collections.EMPTY_LIST;
        }
        List<AsmAttribute> local = new ArrayList<>(getAttributes().size());
        local.addAll(getAttributes());
        Collections.sort(local);
        return local;
    }

    public static class AttrsList extends AbstractIndexedCollection<String, AsmAttribute> implements Serializable {

        protected String getElementKey(AsmAttribute el) {
            return el.getName();
        }

        public AttrsList() {
        }

        protected AttrsList(int size) {
            super(size);
        }

        public AttrsList cloneDeep() {
            AttrsList nlist = new AttrsList(size());
            for (AsmAttribute attr : this) {
                nlist.add(attr.cloneDeep());
            }
            return nlist;
        }
    }

    /**
     * Perform deep clone of this assembly.
     * Cloned parents are cached in <c>cloneContext</c>.
     */
    public Asm cloneDeep(Map<String, Asm> cloneContext) {
        Asm asm = cloneContext.get(name);
        if (asm != null) {
            return asm;
        }
        asm = new Asm();
        asm.id = id;
        asm.name = name;
        asm.hname = hname;
        asm.description = description;
        asm.options = options;
        asm.published = published;
        asm.template = template;
        asm.controller = controller;
        asm.core = (core != null) ? core.cloneDeep() : null;
        asm.cdate = cdate;
        asm.mdate = mdate;
        asm.edate = edate;
        asm.attributes = (attributes != null) ? attributes.cloneDeep() : null;
        asm.navParentId = navParentId;
        asm.navAlias = navAlias;
        asm.navCachedPath = navCachedPath;
        if (getParents() != null) {
            asm.parents = new ArrayList<>(getParents().size());
            for (Asm parent : getParents()) {
                asm.parents.add(parent.cloneDeep(cloneContext));
            }
        }
        cloneContext.put(asm.name, asm);
        return asm;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !Asm.class.isAssignableFrom(o.getClass())) {
            return false;
        }
        return Objects.equals(name, ((Asm) o).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Asm{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", hname='").append(hname).append('\'');
        sb.append(", options='").append(options).append('\'');
        sb.append(", core=").append(core);
        sb.append(", description='").append(description).append('\'');
        sb.append(", navParentId=").append(navParentId);
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }
}
