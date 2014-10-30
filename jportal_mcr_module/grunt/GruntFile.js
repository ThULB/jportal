module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-typescript');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-uglify');

    var config = {
	moduleName : "derivate-link",
	modulePath : "../../src/main/typescript/derivate-link/",
	iviewDependencys : {
	    "base" : "iview-client/META-INF/resources/modules/iview2/js/iview-client-base.d.ts",
	    "mets" : "iview-client/META-INF/resources/modules/iview2/js/iview-client-mets.d.ts"
	}
    };

    var uglifyFiles = function() {
	var uglifyFiles = {};
	uglifyFiles["classes/META-INF/resources/modules/iview2/js/" + config.moduleName
		+ "-module.min.js"] = "classes/META-INF/resources/modules/iview2/js/"
		+ config.moduleName + "-module.js";
	return uglifyFiles;
    }();
    
    var path = require('path');

    var processFiles = function(content, srcpath) {
	/*
	 * Simple dependency injection.
	 * 
	 * config.iviewDependencys contains all dependencies if if a reference
	 * path found wich contains dependencyName it will be replaced with the
	 * d.ts file. You can refer to the original file (for autocomplete of
	 * the ide) and it will be replaced.
	 */

	// Check all line of the file
	var linesOfFile = content.split("\n");
	var contentFilteredReferences = linesOfFile.filter(function(element) {
	    // Filter the lines and remove paths to the original
	    if (element.indexOf("<reference") != -1) {
		var lineOkay = true;
		for ( var dependency in config.iviewDependencys) {
		    lineOkay &= element.indexOf(dependency) == -1;
		}
		return lineOkay;
	    } else {
		return true;
	    }
	}).join("\n");

	// add dependencys
	var dependencys = "";
	for ( var dependency in config.iviewDependencys) {
	    var dependencyPath = config.iviewDependencys[dependency];
	    try {
		dependencys += "\n/// <reference path=\"" + path.resolve(dependencyPath) + "\" />";
	    } catch (e) {
		grunt.log.error(e);
	    }
	}
	return dependencys + "\n" + contentFilteredReferences;
    };

    grunt.initConfig({
	pkg : grunt.file.readJSON('package.json'),
	watch : {
	    ts : {
		files : [ 'src/**/*.ts' ],
		tasks : 'default',
		options : {
		    forever : false,
		    livereload : true
		}
	    }
	},
	typescript : {
	    example : {
		src : "classes/META-INF/resources/modules/iview2/ts/" + config.moduleName + "/module.ts",
		dest : "classes/META-INF/resources/modules/iview2/js/" + config.moduleName + "-module.js",
		options : {
		    module : 'commonjs',
		    target : 'es5',
		    basePath : '',
		    sourceMap : true,
		    declaration : true
		}
	    }
	},
	uglify : {
	    example : {
		files : uglifyFiles,
		options : {
		    mangle : true,
		    sourceMap : true,
		    sourceMapIn : "classes/META-INF/resources/modules/iview2/js/" + config.moduleName
			    + "-module.js.map"
		}
	    }
	},
	copy : {
	    example : {
		expand : true,
		cwd : config.modulePath,
		src : "**",
		dest : "classes/META-INF/resources/modules/iview2/ts/" + config.moduleName,
		options : {
		    processContent : processFiles
		}
	    }

	}
    });
    grunt.event.on('watch', function(action, filepath, target) {
	grunt.task.run('copy');
	grunt.task.run('typescript');
	grunt.task.run('uglify');
    });

    grunt.registerTask('default', function() {
	grunt.task.run('copy');
	grunt.task.run('typescript');
	grunt.task.run('uglify');
    });
};