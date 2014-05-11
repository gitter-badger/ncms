/**
 * Media files selector.
 * File list table with search field.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 */
qx.Class.define("ncms.mmgr.MediaFilesSelector", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {

        /**
         * DATA: {
         *   id : {Integer} File ID
         *   name : {String} File name
         *   content_type : {String} File content type
         *   content_length : {Integer} File data length
         *   folder : {String} Full path to file folder
         *   status : {Integer} 1 - folder, 0 - file
         * }
         * or null
         */
        "fileSelected" : "qx.event.type.Data"
    },

    properties : {

        appearance : {
            refine : true,
            init : "mf-selector"
        },

        constViewSpec : {
            check : "Object",
            nullable : true,
            apply : "__applyConstViewSpec"
        },

        /**
         * Set media item:
         *
         *  item = {
         *        "label"  : {String} Item name.
         *        "status" : {Number} Item status. (1 - folder, 0 - file)
         *        "path"   : {Array} Path to the item (from tree root)
         *  };
         */
        "item" : {
            check : "Object",
            nullable : true,
            apply : "__applyItem"
        }
    },

    construct : function(allowModify, constViewSpec, smodel) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function() {
            this.__search(null);
        }, this);
        sf.addListener("input", function(ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("changeValue", function(ev) {
            this.__search(ev.getData());
        }, this);

        this.__table = new ncms.mmgr.MediaFilesTable().set({
            "statusBarVisible" : true,
            "showCellFocusIndicator" : false});

        if (smodel != null) {
            this.__table.setSelectionModel(smodel);
        }
        this.__table.getSelectionModel().addListener("changeSelection", function(ev) {
            this.__updateState();
            var file = this.__table.getSelectedFile();
            this.fireDataEvent("fileSelected", file ? file : null);
        }, this);


        this.__allowModify = !!allowModify;
        this.__dropFun = this.__handleDropFiles.bind(this);

        this._add(this.__sf);
        this._setupToolbar();
        this._add(this.__table, {flex : 1});

        if (constViewSpec != null) {
            this.setConstViewSpec(constViewSpec);
        }
        this.addListener("appear", function() {
            this.__ensureUploadControls();
            this.__updateState();
        }, this);
    },

    members : {

        __allowModify : false,

        __dropFun : null,

        __sf : null,

        __table : null,

        __rmBt : null,

        setViewSpec : function(vspec) {
            this.__table.getTableModel().setViewSpec(this.__createViewSpec(vspec));
        },

        updateViewSpec : function(vspec) {
            this.__table.getTableModel().updateViewSpec(this.__createViewSpec(vspec));
        },

        reload : function(vspec) {
            this.__table.getTableModel().reloadData();
            this.__table.resetSelection();
        },

        resetSelection : function() {
            this.__table.resetSelection();
        },

        getTable : function() {
            return this.__table;
        },

        _setupToolbar : function() {
            if (!this.__allowModify) {
                return;
            }
            var toolbar = new qx.ui.toolbar.ToolBar();
            var part = new qx.ui.toolbar.Part()
                    .set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            var bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/add.png")
                    .set({"appearance" : "toolbar-table-button"});
            bt.addListener("execute", this.__addFiles, this);
            part.add(bt);

            this.__rmBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/delete.png")
                    .set({"appearance" : "toolbar-table-button"});
            bt.addListener("execute", this.__rmFiles, this);

            part.add(bt);
            this._add(toolbar);
        },

        __createViewSpec : function(vspec) {
            if (this.getConstViewSpec() == null) {
                return vspec;
            }
            var nspec = {};
            qx.Bootstrap.objectMergeWith(nspec, this.getConstViewSpec(), false);
            qx.Bootstrap.objectMergeWith(nspec, vspec, false);
            return nspec;
        },

        __search : function(val) {
            this.__table.resetSelection();
            var vspec = (val != null && val != "" ? {stext : val} : {});
            this.setViewSpec(this.__createViewSpec(vspec));
        },

        __applyItem : function(item) {
            if (item != null && item["status"] == 1) { //folder
                var folder = "/" + item["path"].join("/");
                this.setConstViewSpec({"folder" : folder, "status" : 0});
            } else {
                this.setConstViewSpec({"status" : 0});
            }
        },

        __applyConstViewSpec : function() {
            this.__search();
        },

        __addFiles : function(ev) {
            qx.log.Logger.info("addFiles!!!");
        },

        __rmFiles : function(ev) {
            qx.log.Logger.info("rmFiles!!!");
        },

        __updateState : function() {
            var selected = !this.__table.getSelectionModel().isSelectionEmpty();
            if (this.__rmBt) {
                this.__rmBt.setEnabled(selected);
            }
        },

        __handleDropFiles : function(ev) {
            ev.stopPropagation();
            ev.preventDefault();
            var files = ev.dataTransfer.files;
            var path = (this.getItem() != null) ? this.getItem()["path"] : [];
            var dlg = new sm.ui.upload.FileUploadProgressDlg(function(f) {
                return ncms.Application.ACT.getRestUrl("media.upload", path.concat(f.name));
            }, files);
            dlg.addListener("completed", function() {
                dlg.close();
                this.reload();
            }, this);
            dlg.open();
        },

        __ensureUploadControls : function() {
            if (!this.__allowModify) { //we are in read-only mode
                return;
            }
            var el = this.getContentElement().getDomElement();
            if (el.ondrop == this.__dropFun) {
                return;
            }
            el.ondrop = this.__dropFun;
            el.ondragover = function() {
                return false;
            };
        }
    },

    destruct : function() {
        if (this.getContentElement() != null) {
            var el = this.getContentElement().getDomElement();
            el.ondrop = null;
            el.ondragover = null;
        }
        this.__sf = null;
        this.__table = null;
        this.__dropFun = null;
        this.__rmBt = null;
    }
});