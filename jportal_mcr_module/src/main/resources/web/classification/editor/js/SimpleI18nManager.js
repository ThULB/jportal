/**
 * Localisation Manager
 */
var SimpleI18nManager = (function() {

	var instance = null;
	var supportedLanguages = undefined;
	var currentLanguage = "de";

	function CreateI18nManager() {

		this.initialize = function(/*Array*/ langArr, /*String*/ currentLang) {
			if(langArr.length <= 0) {
				console.log("Empty language array. Couldn't initialize I18nManager!");
				return;
			}
			supportedLanguages = langArr;
			this.setCurrentLanguage(currentLang);
		}

		this.getSupportedLanguages = function() {
			return supportedLanguages;
		}

		this.setCurrentLanguage = function(/*String*/ newLang) {
			if(!this.isSupportedLanguage(newLang)) {
				console.log("'" + newLang + "' is not supported! Valid languages are: " + supportedLanguages);
				return;
			}
			currentLanguage = newLang;
		}

		this.getCurrentLanguage = function() {
			return currentLanguage;
		}

		this.addSupportedLanguage = function(/*String*/ newLang) {
			supportedLanguages.push(newLang);
		}

		this.isSupportedLanguage = function(/*String*/ lang) {
			return dojo.indexOf(supportedLanguages, lang) >= 0;
		}
	}

	return new function() {
		this.getInstance = function() {
			if(instance == null) {
				instance = new CreateI18nManager();
				instance.constructor = null;
			}
			return instance;
		}
	}
})();
