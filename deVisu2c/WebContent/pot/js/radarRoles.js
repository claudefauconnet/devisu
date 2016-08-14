var background;
var roleSelects;
function initRadarRoles() {
	$("#radarRolesDiv").css("visibility", "visible");

	xmlDoc = getXmlDoc("data/" + dbName + ".xml");
	var fields = XML_getFieldNames(xmlDoc);
	fields.splice(0,0,"");
	background = xmlDoc.getElementsByTagName("background")[0];
	if (!background){
		background = xmlDoc.createElement("background");
		var radarElt=xmlDoc.childNodes[0];
		radarElt.appendChild(background);
	}
	printXml();
	//xmlDoc.appendChild(background);
	 roleSelects = [radarRoleLselect, radarRoleHselect, radarRoleRselect, radarRoleCselect, radarRoleSselect, radarRoleBgTypeSelect, radarRoleBgHorderSelect,radarRoleBgHnumSelect, radarRoleBgRorderSelect, radarRoleBgRnumSelect, radarRoleColorStartSelect, radarRoleColorEndSelect ];
	
	 
	fillSelectOptions(radarRoleHselect, fields);
	fillSelectOptions(radarRoleRselect, fields);
	fillSelectOptions(radarRoleLselect, fields);
	fillSelectOptions(radarRoleCselect, fields);
	fillSelectOptions(radarRoleSselect, fields);
	initRoles();

}
function radarRoleAction(input) {
	if (input.textContent == "OK") {
		if (updateRoles()){
			printXml();
			var s = new XMLSerializer();
			 var str = s.serializeToString(xmlDoc);
			proxy_saveRadarXml(dbName,str);
			$("#radarRolesDiv").css("visibility", "hidden");
			//window.location.href=updateQueryStringParameter(window.location.href, "dbName", dbName);
		}
		
		return;
	}

	if (input.textContent == "Cancel") {
		$("#radarRolesDiv").css("visibility", "hidden");
		return;
	}

}

function updateRoles() {
	for ( var i = 0; i < roleSelects.length; i++) {
		var select = roleSelects[i];
		var value = $(select).val();
		if (i < 4 && (!value || value == "")) {
			alert(roleSelects + "value is mandatory");
			return false;
		}
		var field = XML_getField(value);
		if(!field && select==radarRoleSselect)
			continue;
		if (select == radarRoleHselect) {
			field.setAttribute("radarRole", "horizontalAxis");
		}
		if (select == radarRoleRselect) {
			field.setAttribute("radarRole","radialAxis");
		}
		if (select == radarRoleLselect) {
			field.setAttribute("radarRole","label");
		}
		if (select == radarRoleCselect) {
			field.setAttribute("radarRole","color");
		}
		if (select == radarRoleSselect) {
			field.setAttribute("radarRole","shape");
		}

		// ************BG*******************
		if (select == radarRoleBgTypeSelect) {
			background.setAttribute("type", value);
		}
		if (select == radarRoleBgHnumSelect) {
			background.setAttribute("number-horizontal-steps", parseInt(value));
		}
		if (select == radarRoleBgRnumSelect) {
			background.setAttribute("number-radial-steps", parseInt(value));
		}
		if (select == radarRoleColorStartSelect) {
			background.setAttribute("start-color", value);
		}
		if (select == radarRoleColorEndSelect) {
			background.setAttribute("end-color", value);
		}
		if (select == radarRoleBgHorderSelect) {
			background.setAttribute("order-horizontal-steps", parseInt(value));
		}
		if (select == radarRoleBgRorderSelect) {
			background.setAttribute("order-radial-steps", parseInt(value));
		}
		if (select == radarRoleBgHorderSelect) {
			background.setAttribute("order-horizontal-steps", value);
		}
		if (select == radarRoleBgRorderSelect) {
			background.setAttribute("order-radial-steps", value);
		}

	}

	return true;

}

function initRoles() {

	
	for ( var i = 0; i < roleSelects.length; i++) {
		var select = roleSelects[i];

		if (select == radarRoleHselect) {
			initInput(select,"horizontalAxis")
		}
		if (select == radarRoleRselect) {
			initInput(select,"radialAxis");
		}
		if (select == radarRoleLselect) {
			initInput(select,"label");
		}
		if (select == radarRoleCselect) {
			initInput(select,"color");
		}
		if (select == radarRoleSselect) {
			initInput(select,"shape");
		}

		// ************BG*******************
		if (select == radarRoleBgTypeSelect) {
			initBackground(select,"type");
		}
		if (select == radarRoleBgHnumSelect) {
			initBackground(select,"number-horizontal-steps");
		}
		if (select == radarRoleBgRnumSelect) {
			initBackground(select,"number-radial-steps");
		}
		if (select == radarRoleColorStartSelect) {
			initBackground(select,"start-color");
		}
		if (select == radarRoleColorEndSelect) {
			initBackground(select,"end-color");
		}
		if (select == radarRoleBgHorderSelect) {
			initBackground(select,"order-horizontal-steps");
		}
		if (select == radarRoleBgRorderSelect) {
			initBackground(select,"order-radial-steps");
		}
	

	}
}

function initInput(input,role){
	fieldName=XML_getFieldForRole(role);
	if(fieldName!=null && fieldName!="")
		$(input).val(fieldName);
}

function initBackground(input,attr){
	attrVal=background.getAttribute(attr);
	if(attrVal!=null && attrVal!="" )
		$(input).val(attrVal);
}

function printXml(){
	var s = new XMLSerializer();
	 var str = s.serializeToString(xmlDoc);
	 console.log(str);
}

function updateQueryStringParameter(uri, key, value) {
	  var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
	  var separator = uri.indexOf('?') !== -1 ? "&" : "?";
	  if (uri.match(re)) {
	    return uri.replace(re, '$1' + key + "=" + value + '$2');
	  }
	  else {
	    return uri + separator + key + "=" + value;
	  }
	}
