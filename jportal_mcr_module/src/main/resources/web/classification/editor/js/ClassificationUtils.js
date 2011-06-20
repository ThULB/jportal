/**
 * Clones an object. A new instance is generated
 * 
 * @param obj object to clone
 * @return new instance of the object
 */
function clone(obj) {
	if (obj == null || typeof (obj) != 'object')
		return obj;
	var temp = new obj.constructor(); // changed (twice)

	for (var key in obj)
		temp[key] = clone(obj[key]);
	return temp;
}

function deepEquals(a, b) {
	var result = true;

	function lengthTest(a, b) {
		var count = 0;
		for( var p in a)
			count++;
		for( var p in b)
			count--;
		return count == 0 ? true: false;
	}

	function typeTest(a, b) {
		return (typeof a == typeof b);
	}

	function test(a, b) {
		if (!typeTest(a, b))
			return false;
		if (typeof a == 'function' || typeof a == 'object') {
			if(!lengthTest(a,b))
				return false;
			for ( var p in a) {
				result = test(a[p], b[p]);
				if (!result)
					return false;
			}
			return result;
		}
		return (a == b);
	}
	return test(a, b);
}

function getClassificationId(/*String*/ id) {
	var index = id.indexOf(".");
	if(index == -1)
		return id;
	return id.substring(0, index);
}

function getCategoryId(/*String*/ id) {
	var index = id.indexOf(".");
	if(index == -1)
		return "";
	return id.substring(index + 1);
}
