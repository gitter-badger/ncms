/**
 * RichRefAM value editor.
 * @asset (ncms/icon/16/misc/chain-plus.png)
 */
qx.Class.define("ncms.asm.am.RichRefAMValueWidget", {
    extend : qx.ui.core.Widget,
    implement : [ qx.ui.form.IModel,
                  ncms.asm.am.IValueWidget],
    include : [ ncms.asm.am.MValueWidget ],

    events : {

    },

    properties : {
        model : {
            check : "Object",
            nullable : true,
            event : "changeModel",
            apply : "__applyModel"
        }
    },

    construct : function(attrSpec, asmSpec) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.addState("widgetNotReady");

        var opts = ncms.Utils.parseOptions(attrSpec["options"]);
        //qx.log.Logger.info("OPTS=" + JSON.stringify(opts));

        var el;
        var form = this.__form = new qx.ui.form.Form();
        var bf = this.__bf = new sm.ui.form.ButtonField(null, "ncms/icon/16/misc/chain-plus.png", true);
        bf.setReadOnly(true);
        bf.setRequired(true);
        bf.addListener("execute", this.__onSetLink, this);
        bf.addListener("changeValue", this.__modified, this);
        form.add(bf, this.tr("Link"), null, "link");

        if (opts["allowDescription"] === "true") {
            el = new qx.ui.form.TextArea();
            el.setRequired(true);
            el.setMaxLength(1024); //todo
            el.addListener("input", this.__modified, this);
            form.add(el, this.tr("Description"), null, "description");

        }
        if (opts["allowImage"] === "true") {
            var iAttrSpec = sm.lang.Object.shallowClone(attrSpec);
            iAttrSpec["hasLargeValue"] = false;
            iAttrSpec["value"] = "null";
            iAttrSpec["options"] = opts["image"];
            iAttrSpec["required"] = true;
            this.__imageAM = new ncms.asm.am.ImageAM();
            var iw = this.__imageAM.activateValueEditorWidget(iAttrSpec, asmSpec);
            var validator = null;
            if (typeof iw.getUserData("ncms.asm.validator") === "function") {
                validator = iw.getUserData("ncms.asm.validator");
            } else if (typeof iw.getValidator === "function") {
                validator = iw.getValidator();
            }
            iw.addListener("modified", this.__modified, this);
            form.add(iw, this.tr("Image"), validator, "image", iw);
        }

        var fr = new sm.ui.form.FlexFormRenderer(form);
        this._add(fr);
    },

    members : {

        __bf : null,

        __imageAM : null,

        __form : null,

        __modified : function() {
            if (this.hasState("widgetNotReady")) {
                return;
            }
            this.fireEvent("modified");
        },

        __onSetLink : function(ev) {
            var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Please set resource link"), {
                allowExternalLinks : true
            });
            dlg.addListener("completed", function(ev) {
                var data = ev.getData();
                var val = [];
                if (!sm.lang.String.isEmpty(data["externalLink"])) {
                    val.push(data["externalLink"]);
                } else {
                    val.push("page:" + sm.lang.Array.lastElement(data["guidPath"]));
                }
                if (!sm.lang.String.isEmpty(data["linkText"])) {
                    val.push(data["linkText"]);
                }
                this.__bf.setValue(val.join(" | "));
                this.__bf.setUserData("data", data);
                dlg.close();
            }, this);
            dlg.open();
        },


        __applyModel : function(model) {
            model = model || {};
            //qx.log.Logger.info("Apply model " + JSON.stringify(model));
            this.addState("widgetNotReady");
            var items = this.__form.getItems();
            // {
            //      "image":{"id":561,"options":{"restrict":"false","width":"693","skipSmall":"false","resize":"true"}},
            //       "link":"page:e96b3224e0ef7850e6c86d6d857b327b | Главная","description":"test"
            // }
            if (items["image"] && model["image"] != null) {
                items["image"].setModel(model["image"]);
            }
            if (model["link"] != null) {
                this.__bf.setValue(model["link"]);
            } else {
                this.__bf.resetValue();
            }
            if (items["description"]) {
                if (model["description"] != null) {
                    items["description"].setValue(model["description"]);
                } else {
                    items["description"].resetValue();
                }
            }
            this.removeState("widgetNotReady");
        },

        valueAsJSON : function() {
            if (!this.__form.validate() || this.__bf.getUserData("data") == null) {
                return null;
            }
            var data = {};
            if (this.__imageAM) {
                data["image"] = this.__imageAM.valueAsJSON();
            }
            var items = this.__form.getItems();
            data["link"] = items["link"].getValue();
            if (items["description"]) {
                data["description"] = items["description"].getValue();
            }
            data["name"] = this.__bf.getUserData("data")["linkText"];
            return data;
        }

    },

    destruct : function() {
        this.__bf = null;
        this._disposeObjects("__form", "__imageAM");
    }
});