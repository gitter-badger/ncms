/**
 * Wiki editor
 *
 * @asset(ncms/icon/16/wiki/note_add.png)
 * @asset(ncms/icon/16/wiki/text_heading_1.png)
 * @asset(ncms/icon/16/wiki/text_heading_2.png)
 * @asset(ncms/icon/16/wiki/text_heading_3.png)
 * @asset(ncms/icon/16/wiki/text_bold.png)
 * @asset(ncms/icon/16/wiki/text_italic.png)
 * @asset(ncms/icon/16/wiki/text_list_bullets.png)
 * @asset(ncms/icon/16/wiki/text_list_numbers.png)
 * @asset(ncms/icon/16/wiki/link_add.png)
 * @asset(ncms/icon/16/wiki/image_add.png)
 * @asset(ncms/icon/16/wiki/table_add.png)
 * @asset(ncms/icon/16/wiki/tree_add.png)
 * @asset(ncms/icon/16/wiki/note_add.png)
 */
qx.Class.define("ncms.editor.wiki.WikiEditor", {
    extend : qx.ui.core.Widget,
    implement : [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include : [
        qx.ui.form.MForm,
        qx.ui.core.MChildrenHandling
    ],


    statics : {
        createTextSurround : function(text, level, pattern, trails) {
            var nval = [];

            var hfix = qx.lang.String.repeat(pattern, level < 1 ? 1 : level);
            nval.push(hfix);
            nval.push(trails || "");
            nval.push(text);
            nval.push(trails || "");
            nval.push(hfix);

            return nval.join("");
        }
    },

    events : {
    },

    properties : {
        "type" : {
            check : ["mediaWiki", "markdown"],
            init : "mediaWiki",
            apply : "_applyType"
        }
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(4));

        this.__editorControls = []; // cache for controls

        this.getChildControl("toolbar");
        var ta = this.getChildControl("textarea");

        //todo scary textselection hacks
        if (qx.core.Environment.get("engine.name") == "mshtml") {
            var getCaret = function(el) {
                if (el == null) {
                    return 0;
                }
                var start = 0;
                var range = el.createTextRange();
                var range2 = document.selection.createRange().duplicate();
                // get the opaque string
                var range2Bookmark = range2.getBookmark();
                range.moveToBookmark(range2Bookmark);
                while (range.moveStart("character", -1) !== 0) {
                    start++
                }
                return start;
            };
            var syncSel = function() {
                var tael = ta.getContentElement().getDomElement();
                this.__lastSStart = this.__lastSEnd = getCaret(tael);
            };

            ta.addListener("keyup", syncSel, this);
            ta.addListener("focus", syncSel, this);
            ta.addListener("click", syncSel, this);
        }
    },

    members : {
        __lastToolbarItem : null,

        __editorControls : null,

        __lastSStart : 0,

        __lastSEnd : 0,

        addListener : function(type, listener, self, capture) {
            switch (type) {
                default:
                    //todo scary hack
                    this._getTextArea().addListener(type, listener, self, capture);
                    break;
            }
        },

        _getTextArea : function() {
            return this.getChildControl("textarea");
        },

        // overridden
        setValue : function(value) {
            this._getTextArea().setValue(value);
        },

        // overridden
        resetValue : function() {
            this._getTextArea().resetValue();
        },

        // overridden
        getValue : function() {
            return this._getTextArea().getValue();
        },

        //overriden
        _applyEnabled : function(value, old) {
            this.base(arguments, value, old);
            this._getTextArea().setEnabled(value);
        },

        _applyType : function(value, old) {
            for (var i = 0; i < this.__editorControls.length; ++i) {
                this.__updateControl(this.__editorControls[i]);
            }
        },

        //overriden
        _createChildControlImpl : function(id) {
            var control;
            switch (id) {
                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar().set({overflowHandling : true, "show" : "icon"});
                    this._add(control, {flex : 0});
                    this.__lastToolbarItem = control.addSpacer();
                    var overflow = new qx.ui.toolbar.MenuButton(this.tr("More..."));
                    overflow.setMenu(new qx.ui.menu.Menu());
                    control.add(overflow);
                    control.setOverflowIndicator(overflow);
                    this.__initToolbar(control);
                    break;

                case "textarea":
                    control = new qx.ui.form.TextArea();
                    this._add(control, {flex : 1});
                    break;
            }

            return control || this.base(arguments, id);
        },

        /**
         * Add new toolbar control.
         * @param options control configuration:
         * {
         *  "id" : String, optional. Uses for showing/excluding controls
         *  "title" : String. optional. Title for control in toobar overflow menu
         *  "icon" : String. required. Icon for control in toobar and toolbar overflow menu
         *  "tooltipText" : String. optional. Text for control tooltip
         *  "prompt" : Function. optional. Callback for prompt additional params by user
         *          function(cb, editor, stext) {}
         *                  - cb : function(promptData) - callback for chain
         *                  - editor - current WikiEditor instance
         *                  - stext - selected text
         *  "insert<type>": Function. optional-required. Callback for processing prompt data and modifying editor text.
         *         function(cb, promptData) {}
         *                  - cb : function(text) - callback for chain. text will be inserted instead selected text
         *                  - promptData - data from prompt function execution (if specifyed) or selected text in other case
         *         <type> - captalized wiki editor type. If function for current editor type not specifyed, control for this type will not be shown.
         * }
         */
        addToolbarControl : function(options) {
            this._addToolbarControl(this.getChildControl("toolbar"), options);
        },

        /**
         * Set "excluded" state for all toolbar controls with given id.
         */
        excludeToolbarControl : function(id) {
            for(var i = 0; i < this.__editorControls.length; ++i) {
                var cmeta = this.__editorControls[i];
                if (cmeta["id"] === id) {
                    cmeta.options["excluded"] = true;
                    this.__updateControl(cmeta);
                }
            }
        },

        /**
         * Reset "excluded" state for all toolbar controls with given id.
         */
        showToolbarControl : function(id) {
            for(var i = 0; i < this.__editorControls.length; ++i) {
                var cmeta = this.__editorControls[i];
                if (cmeta["id"] === id) {
                    cmeta.options["excluded"] = false;
                    this.__updateControl(cmeta);
                }
            }
        },

        /**
         * Reset "excluded" state for all toolbar controls
         */
        resetToolbarControls : function() {
            for(var i = 0; i < this.__editorControls.length; ++i) {
                this.__editorControls[i].options["excluded"] = false;
                this.__updateControl(this.__editorControls[i]);
            }
        },


        _addToolbarControl : function(toolbar, options) {
            var callback = this.__buildToolbarControlAction(options);

            var cmeta = this.__editorControls[this.__editorControls.length] = {
                options : options,
                buttons : []
            };

            cmeta.buttons[0] = this.__createToolbarControl(toolbar, this.__lastToolbarItem, qx.ui.toolbar.Button, callback, options);
            cmeta.buttons[0].set({marginLeft : 0, marginRight : 0});
            if (toolbar.getOverflowIndicator()) {
                cmeta.buttons[1] = this.__createToolbarControl(toolbar.getOverflowIndicator().getMenu(), null, qx.ui.menu.Button, callback, options);
            }

            this.__updateControl(cmeta);
        },

        __updateControl : function(cmeta) {
            var applied = !!cmeta.options[("insert" + qx.lang.String.capitalize(this.getType()))] && !cmeta.options["excluded"];
            for (var i = 0; i < cmeta.buttons.length; ++i) {
                if (applied) {
                    cmeta.buttons[i].show();
                } else {
                    cmeta.buttons[i].exclude();
                }
            }
        },

        __buildToolbarControlAction : function(options) {
            var me = this;
            return function() {
                var icb = options[("insert" + qx.lang.String.capitalize(me.getType()))];
                if (!icb) {
                    return;
                }

                var selectedText = this._getTextArea().getContentElement().getTextSelection();
                if (options["prompt"]) {
                    options["prompt"].call(this, function(text) {
                        icb.call(me, me._insertText, text);
                    }, this, selectedText);
                } else {
                    icb.call(me, me._insertText, selectedText);
                }
            };
        },

        __createToolbarControl : function(toolbar, before, btclass, callback, options) {
            var bt = new btclass(options["title"], options["icon"]);
            if (options["tooltip"]) {
                bt.setToolTip(new qx.ui.tooltip.ToolTip(options["tooltipText"]));
            }
            bt.addListener("execute", callback, this);

            if (before) {
                toolbar.addBefore(bt, before);
            } else {
                toolbar.add(bt);
            }

            return bt;
        },

        __initToolbar : function(toolbar) {
            if (true/* TODO: wiki help */) {
                var helpButton = new qx.ui.toolbar.Button(this.tr("Help"), "ncms/icon/16/help/help.png");
                toolbar.addAfter(helpButton, this.__lastToolbarItem);
                helpButton.addListener("execute", function() {
                    ncms.Application.alert("TODO: help");
                });
            }

            var cprompt = function(title) {
                return function(cb, editor, sText) {
                    if (!sText) {
                        sText = prompt(title);
                    }
                    if (sText != null && sText != undefined) {
                        cb.call(this, sText);
                    }
                }
            };
            var csurround = function(level, pattern, trails) {
                return function(cb, data) {
                    cb.call(this, ncms.editor.wiki.WikiEditor.createTextSurround(data, level, pattern, trails));
                }
            };
            var cscall = function(func) {
                return function(cb, data) {
                    cb.call(this, func.call(this, data));
                }
            };

            this._addToolbarControl(toolbar, {
                id : "H1",
                icon : "ncms/icon/16/wiki/text_heading_1.png",
                tooltipText : this.tr("Heading 1"),
                prompt : cprompt(this.tr("Header text")),
                insertMediaWiki : csurround(1, "=", " "),
                insertMarkdown : csurround(1, "#", " ")
            });
            this._addToolbarControl(toolbar, {
                id : "H2",
                icon : "ncms/icon/16/wiki/text_heading_2.png",
                tooltipText : this.tr("Heading 2"),
                prompt : cprompt(this.tr("Header text")),
                insertMediaWiki : csurround(2, "=", " "),
                insertMarkdown : csurround(2, "#", " ")
            });
            this._addToolbarControl(toolbar, {
                id : "H3",
                icon : "ncms/icon/16/wiki/text_heading_3.png",
                tooltipText : this.tr("Heading 3"),
                prompt : cprompt(this.tr("Header text")),
                insertMediaWiki : csurround(3, "=", " "),
                insertMarkdown : csurround(3, "#", " ")
            });
            this._addToolbarControl(toolbar, {
                id : "Bold",
                icon : "ncms/icon/16/wiki/text_bold.png",
                tooltipText : this.tr("Bold"),
                prompt : cprompt(this.tr("Bold text")),
                insertMediaWiki : csurround(1, "'", ""),
                insertMarkdown : csurround(2, "*", "")
            });
            this._addToolbarControl(toolbar, {
                id : "Italic",
                icon : "ncms/icon/16/wiki/text_italic.png",
                tooltipText : this.tr("Italic"),
                prompt : cprompt(this.tr("Italics text")),
                insertMediaWiki : csurround(2, "'", ""),
                insertMarkdown : csurround(1, "*", "")
            });

            this._addToolbarControl(toolbar, {
                id : "UL",
                icon : "ncms/icon/16/wiki/text_list_bullets.png",
                tooltipText : this.tr("Bullet list"),
                insertMediaWiki : cscall(this.__mediaWikiUL),
                insertMarkdown : cscall(this.__markdownUL)
            });
            this._addToolbarControl(toolbar, {
                id : "OL",
                icon : "ncms/icon/16/wiki/text_list_numbers.png",
                tooltipText : this.tr("Numbered list"),
                insertMediaWiki : cscall(this.__mediaWikiOL),
                insertMarkdown : cscall(this.__markdownOL)
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/link_add.png",
                tooltipText : this.tr("Link to another page")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/image_add.png",
                tooltipText : this.tr("Add image|link to file")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/table_add.png",
                tooltipText : this.tr("Add table")
            });
            this._addToolbarControl(toolbar, {
                id : "Tree",
                icon : "ncms/icon/16/wiki/tree_add.png",
                tooltipText : this.tr("Add tree"),
                insertMediaWiki : cscall(this.__mediaWikiTree)
            });
            this._addToolbarControl(toolbar, {
                id : "Note",
                icon : "ncms/icon/16/wiki/note_add.png",
                tooltipText : this.tr("Create note"),
                insertMediaWiki : cscall(this.__mediaWikiNote),
                insertMarkdown : cscall(this.__markdownNote)
            });
        },

        _getSelectionStart : function() {
            var sStart = this._getTextArea().getTextSelectionStart();
            return (sStart == null || sStart == -1 || sStart == 0) ? this.__lastSStart : sStart;
        },

        _getSelectionEnd : function() {
            var sEnd = this._getTextArea().getTextSelectionEnd();
            return (sEnd == null || sEnd == -1 || sEnd == 0) ? this.__lastSEnd : sEnd;
        },

        _insertText : function(text) {
            var ta = this._getTextArea();
            var tel = ta.getContentElement();
            var scrollY = tel.getScrollY();

            var sStart = this._getSelectionStart();
            var sEnd = this._getSelectionEnd();

            var nval = [];
            var value = ta.getValue();
            if (value == null) value = "";

            nval.push(value.substring(0, sStart));
            nval.push(text);
            nval.push(value.substring(sEnd));
            ta.setValue(nval.join(""));

            var finishPos = sStart + text.length;
            ta.setTextSelection(finishPos, finishPos);
            tel.scrollToY(scrollY);
        },

        //////////////////////////////////////////////////////////////////////////
        /////////////////////////   Helpers    ///////////////////////////////////
        //////////////////////////////////////////////////////////////////////////
        __mediaWikiUL : function(data) {
            var val = [];
            val.push("");
            val.push("* Один");
            val.push("* Два");
            val.push("** Первый у второго");
            val.push("* Три");
            val.push("");
            return val.join("\n");
        },

        __markdownUL : function(data) {
            var val = [];
            val.push("");
            val.push("* Один");
            val.push("* Два");
            val.push("    * Первый у второго");
            val.push("* Три");
            val.push("");
            return val.join("\n");
        },

        __mediaWikiOL : function(data) {
            var val = [];
            val.push("");
            val.push("# Один");
            val.push("# Два");
            val.push("## Первый у второго");
            val.push("# Три");
            val.push("");
            return val.join("\n");
        },

        __markdownOL : function(data) {
            var val = [];
            val.push("");
            val.push("1. Один");
            val.push("1. Два");
            val.push("    1. Первый у второго");
            val.push("1. Три");
            val.push("");
            return val.join("\n");
        },

        __mediaWikiTree : function(data) {
            var val = [];
            val.push("");
            val.push("<tree open=\"true\">");
            val.push("- Корень");
            val.push("-- Потомок 1");
            val.push("--- Потомок третьего уровня");
            val.push("-- Потомок 2");
            val.push("</tree>");
            val.push("");
            return val.join("\n");
        },

        __mediaWikiNote : function(data) {
            var val = [];
            val.push("");
            val.push("<note>");
            val.push("Текст заметки");
            val.push("</note>");
            val.push("");
            return val.join("\n");
        },

        __markdownNote : function(data) {
            var val = [];
            val.push("");
            val.push("<note>");
            val.push("Текст заметки");
            val.push("</note>");
            val.push("");
            return val.join("\n");
        }
    },

    destruct : function() {
        this._disposeArray("__editorControls");
    }
});