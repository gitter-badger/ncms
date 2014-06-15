/**
 * Virtual table of assemblies.
 */
qx.Class.define("ncms.asm.AsmTable", {
    extend : sm.table.Table,

    construct : function(useColumns) {
        var tm = new sm.model.RemoteVirtualTableModel({
            "name" : this.tr("Name"),
            "type" : this.tr("Type"),
            "description" : this.tr("Description")
        }).set({
                    "useColumns" : useColumns || ["name", "type", "description"],
                    "rowdataUrl" : ncms.Application.ACT.getUrl("asms.select"),
                    "rowcountUrl" : ncms.Application.ACT.getUrl("asms.select.count")
                });

        var custom = {
            tableColumnModel : function(obj) {
                return new qx.ui.table.columnmodel.Resize(obj);
            }
        };

        this.base(arguments, tm, custom);

        var rr = new sm.table.renderer.CustomRowRenderer();
        var colorm = qx.theme.manager.Color.getInstance();
        rr.setBgColorInterceptor(qx.lang.Function.bind(function(rowInfo) {
            return colorm.resolve("background");
        }, this));
        this.setDataRowRenderer(rr);


        var tcm = this.getTableColumnModel();
        var cInd = tm.getColumnIndexById("name");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "2*");
        }
        cInd = tm.getColumnIndexById("type");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "1*");
        }
        cInd = tm.getColumnIndexById("description");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "3*");
        }
    },

    members : {

        getSelectedAsmInd : function() {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedAsm : function() {
            var sind = this.getSelectedAsmInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedAsms : function() {
            var me = this;
            var asms = [];
            this.getSelectionModel().iterateSelection(function(ind) {
                asms.push(me.getTableModel().getRowData(ind));
            });
            return asms;
        },

        cleanup : function() {
            this.getTableModel().cleanup();
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});