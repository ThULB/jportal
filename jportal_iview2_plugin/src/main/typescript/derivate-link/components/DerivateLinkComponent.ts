
module mycore.iview.imageviewer.components {

    export class DerivateLinkComponent extends IviewComponent {

        public _toolbarModel: model.MyCoReBasicToolbarModel;
        public _derivateLinkControllGroup;
        public _derivateLinkButton: widgets.toolbar.ToolbarButton;
        public _currentImage: string;

        constructor(private _settings: MyCoReImageViewerSettings) {
            super();
        }

        public handle(e: mycore.iview.widgets.events.IviewEvent): void {
            // add to toolbar
            if (e.type == events.ProvideToolbarModelEvent.TYPE) {
                this._toolbarModel = (<events.ProvideToolbarModelEvent>e).model;
                this._derivateLinkControllGroup = new widgets.toolbar.ToolbarGroup("DerivateLinkControllGroup");
                this._toolbarModel.addGroup(this._derivateLinkControllGroup);
                this._derivateLinkButton = new widgets.toolbar.ToolbarButton("DerivateLinkButton", "", "paperclip", "paperclip");
                this._derivateLinkControllGroup.addComponent(this._derivateLinkButton);
            }
            // i18n
            if (this._derivateLinkButton != null) {
                if (e.type == events.LanguageModelLoadedEvent.TYPE) {
                    var languageModelLoadedEvent = <events.LanguageModelLoadedEvent>e;
                    this.i18n(languageModelLoadedEvent.languageModel);
                }
            }
            // button events
            if (e.type == mycore.iview.widgets.toolbar.events.ButtonPressedEvent.TYPE) {
                var buttonPressedEvent = <mycore.iview.widgets.toolbar.events.ButtonPressedEvent> e;
                if (buttonPressedEvent.button.id == "DerivateLinkButton") {
                    this._linkImage();
                }
            }
            // image change event
            if (e.type == events.ImageChangedEvent.TYPE) {
                this._currentImage = (<events.ImageChangedEvent>e).image.href;
            }
        }

        public i18n(model: model.LanguageModel) {
            this._derivateLinkButton.tooltip = model.getTranslation("toolbar.derivateLink");
        }

        public get handlesEvents(): string[] {
            var handleEvents: Array<string> = new Array<string>();
            handleEvents.push(events.ProvideToolbarModelEvent.TYPE);
            handleEvents.push(events.LanguageModelLoadedEvent.TYPE);
            handleEvents.push(mycore.iview.widgets.toolbar.events.ButtonPressedEvent.TYPE);
            handleEvents.push(events.ImageChangedEvent.TYPE);
            return handleEvents;
        }

        public init() {
            this._currentImage = this._settings.startImage;
            this.trigger(new events.WaitForEvent(this, events.ProvideToolbarModelEvent.TYPE));
            this.trigger(new events.WaitForEvent(this, events.LanguageModelLoadedEvent.TYPE));
            
        }

        private _linkImage() {
            this._derivateLinkButton.active = true;
            var servletPath = this._settings.webApplicationBaseURL + "rsc/derivate/link/bookmark/"
                + this._settings.derivate + "?image=" + this._currentImage;
            jQuery.post(servletPath).fail(function(error) {
                alert(error.responseText);
            });

        }

    }

}

addIviewComponent(mycore.iview.imageviewer.components.DerivateLinkComponent);