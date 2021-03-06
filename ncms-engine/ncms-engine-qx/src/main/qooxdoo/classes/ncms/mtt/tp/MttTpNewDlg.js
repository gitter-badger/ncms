/**
 * `New mtt tracking pixel` popup dialog.
 */
qx.Class.define("ncms.mtt.tp.MttTpNewDlg", {
    extend: sm.ui.form.BaseSavePopupDlg,

    construct: function () {
        this.base(arguments);
    },

    members: {

        _configureForm: function () {
            var page = new qx.ui.form.TextField().set({allowGrowY: true, maxLength: 64, required: true});
            page.addListener("keypress", function (ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            this._form.add(page, this.tr("Name"), null, "name");
            page.focus();
        },

        _save: function (cb) {
            var fitems = this._form.getItems();
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.tp.new", {name: fitems["name"].getValue()}),
                "PUT", "application/json");
            req.addListenerOnce("finished", cb);
            req.send(function (resp) {
                this.fireDataEvent("completed", resp.getContent());
            }, this);
        }
    }
});