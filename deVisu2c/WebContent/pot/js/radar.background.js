var RBx, RBy;
var Rwidth;
var zones = [];
var zoneIds = [];
var nHor = 4;
var nRad = 4;
var type;
var startColor;
var endColor;
var xfield;
var rfield;
var xOrder;
var yOrder;
var BackgroundDrawned = false;

function drawBackground(radarDiv) {
	/*
	 * if (BackgroundDrawned) return;
	 */
	BackgroundDrawned = true;

	if (true || !svgRadar) {
		$(radarDiv).html("");
		var w = $(radarDiv).width();
		var h = $(radarDiv).height();
		d3.select("svg").selectAll("*").remove();
		svgRadar = d3.select(radarDiv).append("svg").attr("width", w).attr(
				"height", h);
	}

	xfield = radarXmls[radarModelName].XML_getFieldForRole("horizontalAxis");
	rfield = radarXmls[radarModelName].XML_getFieldForRole("radialAxis");
	var xmlDoc = radarXmls[radarModelName].getRadarXmlDoc();

	var background = xmlDoc.getElementsByTagName("background");
	if (!background || background.length == 0) {
		alert(" no background element in XML config file;");
		return;
	}
	background = background[0];

	startColor = background.getAttribute("start-color");
	endColor = background.getAttribute("end-color");
	nHor = Number(background.getAttribute("number-horizontal-steps"));
	nRad = Number(background.getAttribute("number-radial-steps"));
	type = background.getAttribute("type");
	xOrder = background.getAttribute("order-horizontal-steps");
	yOrder = background.getAttribute("order-radial-steps");

	if (background.getAttribute("order-horizontal-steps") == "ascendant")
		xOrder = 1;
	else
		xOrder = -1;

	if (background.getAttribute("order-radial-steps") == "descendant")
		yOrder = -1;
	else
		yOrder = 1;

	if (!xfield) {
		alert(" no radar role horizontalAxis in XML config file;");
		return;
	}
	if (!rfield) {
		alert(" no radar role radialAxis in XML config file;");
		return;
	}

	Rwidth = $(radarDiv).width();
	Rheight = $(radarDiv).height();
	/*
	 * RBy = document.getElementById("technologies").clientHeight; Rwidth =
	 * document.getElementById("technologies").clientWidth; Rheight =
	 * document.getElementById("technologies").clientHeight;
	 */
	RBy = Rheight;
	RBy = RBy - 20;
	RBx = 20;

	if (type == "radial") {
		drawRadialBackground();
		drawHellZone("radial");
	} else if (type == "cartesian") {
		drawCartesianBackground();
		drawHellZone("cartesian");
	} else {
		alert("no backgound type configured in XML");
	}

}

function drawRadialBackground() {
	zones = [];
	var _nHor = nHor;
	var _nRad = nRad;
	setAxisLabelsRadial();
	// var coefX = 570;
	var coefX = 900 / _nHor * 4;
	var angle0 = 10;
	for (var i = 0; i < _nHor; i++) {
		x1 = log10(i + 1) * coefX;
		x2 = log10(i + 2) * coefX;
		var angle1 = 20;

		for (var j = 1; j < _nRad + 1; j++) {

			var angle2 = angle1 + (70 / _nRad);

			if (j == 0) {
				angle1 = angle0 + (10 / i);
				// console.log(angle1);
				angle0 = angle1;

			}

			if (j == _nRad + 1)
				angle2 = angle2; // - (log10(i+1) * 2);
			// console.log( j+" "+angle1+ " "+angle2+" " + x1 +" "+ x2);
			zones.push(arc(RBx, RBy, angle1, angle2, x1, x2));
			angle1 = angle2;
		}
	}

	var k = 6;
	var q = -1;
	var h = _nHor + 1;
	var r = _nRad;
	var color = "";
	var nc = 0;
	var colors = getColors(startColor, endColor, _nHor);
	for (var i = 0; i < zones.length; i++) {
		zones[i] = roundPath(zones[i]);
		// console.log("----"+i % _nRad) ;
		if (i % _nRad == 0) {
			h = h - 1;
			r = _nRad;
			k = k + 2;
			// color = "#" + k.toString(16) + k.toString(16) + "F";
			color = colors[nc++];

			// console.log(color);
			var hClass = h;
			if (xOrder == 1)
				hClass = nHor - hClass + 1;

			var xt1 = zones[i][0][1] - 20;
			var xt2 = zones[i][0][2] + 10;
			var text1 = svgRadar.append("text").text("" + (hClass)).attr("dy",
					".55em").attr("class", "title").style("fill", "blue");
			text1.attr("transform", function(d) {
				return "translate(" + xt1 + "," + xt2 + ")";
			});

		} else {
			r = r - 1;
		}
		var rClass = r;
		if (yOrder == -1)
			rClass = nRad - rClass + 1;
		if (i % (_nRad) == (_nRad - 1)) {
			// if(i <zones.length-1){
			var xt1 = zones[i][2][1] - 10;
			var xt2 = zones[i][2][2] + 10;
			var textHor = svgRadar.append("text").text("" + (hClass)).attr(
					"dy", ".55em").attr("class", "radarAxisValue").style(
					"fill", "blue");
			textHor.attr("transform", function(d) {
				return "translate(" + xt1 + "," + xt2 + ")";
			});

		}

		// var my_arc=[];
		var path = processPath(zones[i]);
		var my_arc = svgRadar.append("path").attr("d", path).attr("stroke",
				"blue").attr("stroke-width", 1).attr("fill", color);

		var xt1 = zones[i][1][6] + 20;
		var xt2 = zones[i][1][7] - 20;
		var textRad = svgRadar.append("text").text("" + (rClass)).attr("dy",
				".55em").attr("class", "radarAxisValue").style("fill", "blue");
		textRad.attr("transform", function(d) {
			return "translate(" + xt1 + "," + xt2 + ")";
		});

		var p = (i % _nHor);

		var obj = {};
		obj[xfield] = hClass;
		obj[rfield] = rClass;

		zoneIds.push(obj);

	}

}

function drawHellZone(type) {

	var path
	if (type == "radial") {
		path = [ [ 'M', Rwidth - 230, 0 ], [ 'L', Rwidth, 0 ],
				[ 'L', Rwidth, 300 ], [ 'L', Rwidth - 20, 300 ], [ 'Z' ] ];
	} else if (type == "cartesian") {
		path = [ [ 'M', Rwidth -350, 0 ], [ 'L', Rwidth, 0 ],
				[ 'L', Rwidth, 30 ], [ 'L', Rwidth-350, 30 ], [ 'Z' ] ];
		path = [ [ 'M', Rwidth -30, 0 ], [ 'L', Rwidth, 0 ],
					[ 'L', Rwidth, 30 ], [ 'L', Rwidth-30, 30 ], [ 'Z' ] ];
	}
	zones.push(path);
	path = processPath(path);
	var hellZone = svgRadar.append("path").attr("d", path)

	.attr("class", "hellZone").style("fill", "#ffffff").style("stroke",
			"#0000AA").style("stroke-dasharray", ("3, 3")).attr("visibility",
			"visible");
	var obj = {};
	obj[xfield] = "hell";
	obj[rfield] = "hell";
	zoneIds.push(obj);
	// var hellZoneA=hellZone;*/

}

function processPath(zone) {
	var str = "";

	for (var i = 0; i < zone.length; i++) {
		for (var j = 0; j < zone[i].length; j++) {
			str += zone[i][j];
			if (j > 0)
				str += ","

		}
	}
	// console.log(str);
	return str;
}

function drawCartesianBackground() {

	// var coefX = 570;
	var w = (Rwidth - 50) / nHor;
	var h = (Rheight - 70) / nRad;
	RBx += 10;
	RBy -= 10;
	var colors = getColors(startColor, endColor, nHor * nRad);
	var k = 0;
	for (var i = 0; i < nHor; i++) {
		var x = RBx + (i * w);
		var xLabel;
		if (xOrder == -1)
			xLabel = (nHor - i);
		else
			xLabel = (i + 1);

		var text1 = svgRadar.append("text").text(xLabel).attr("dy", ".55em")
				.attr("class", "radarAxisValue").style("fill", "blue");
		text1.attr("transform", function(d) {
			return "translate(" + (x + (w / 2) - 10) + "," + (RBy + 10) + ")";
		});

		for (var j = 1; j < nRad + 1; j++) {
			var yLabel;
			if (yOrder == -1)
				yLabel = (nRad + 1 - (j));
			else
				yLabel = j;
			var y = RBy - (j * h);
			if (i == 0) {
				var text1 = svgRadar.append("text").text(yLabel).attr("dy",
						".55em").attr("class", "radarAxisValue").style("fill",
						"blue");
				text1.attr("transform", function(d) {
					return "translate(" + 15 + "," + (y + (h / 2) - 10) + ")";
				});

			}

			var color = colors[k++];
			var path = [ [ 'M', x, y ], [ 'l', w, 0 ], [ 'l', 0, h ],
					[ 'l', -w, 0 ], [ 'l', 0, -h ] ];
			zones.push(path);

			path = processPath(path);
			var rects = svgRadar.append("svg:path").attr("d", path).attr(
					"stroke", "blue").attr("stroke-width", 1).attr("fill",
					color);

			var obj = {};

			obj[xfield] = xLabel;
			obj[rfield] = yLabel;

			zoneIds.push(obj);
			/*
			 * var rect=rects[j-1]; rect.data("radarId", obj); if (rect) {
			 * rect.click(function(evt) {
			 * 
			 * var offset = $(document.getElementById("technologies")).offset(); //
			 * adjust mouse x/y var mx = event.clientX - offset.left; var my =
			 * event.clientY - offset.top; findIntersections(mx, my, obj);
			 * 
			 * }); }
			 */
		}
	}
	setAxisLabelsCartesian();

}

function log10(val) {
	var x = Math.LN10;
	// x=Math.log(20);
	return Math.log(val) / x;

}

function arc(centerX, centerY, startAngle, endAngle, innerR, outerR) {
	var radians = Math.PI / 180, largeArc = +(endAngle - startAngle > 180);

	outerX1 = centerX + outerR * Math.cos((startAngle - 90) * radians),
			outerY1 = centerY + outerR * Math.sin((startAngle - 90) * radians),
			outerX2 = centerX + outerR * Math.cos((endAngle - 90) * radians),
			outerY2 = centerY + outerR * Math.sin((endAngle - 90) * radians),
			innerX1 = centerX + innerR * Math.cos((endAngle - 90) * radians),
			innerY1 = centerY + innerR * Math.sin((endAngle - 90) * radians),
			innerX2 = centerX + innerR * Math.cos((startAngle - 90) * radians),
			innerY2 = centerY + innerR * Math.sin((startAngle - 90) * radians);

	// build the path array

	var path = [ [ "M", outerX1, outerY1 ], // move to the start point
	[ "A", outerR, outerR, 0, largeArc, 1, outerX2, outerY2 ], // draw the
	// outer edge of
	// the arc
	[ "L", innerX1, innerY1 ], // draw a line inwards to the start of the inner
	// edge of the arc
	[ "A", innerR, innerR, 0, largeArc, 0, innerX2, innerY2 ], // draw the
	// inner arc
	[ "z" ] // close the path
	];
	// console.log(path);
	return path;
};

function findIntersections(x, y) {
	setMessage("--");
	for (var i = 0; i < zones.length; i++) {
		if (Raphael.isPointInsidePath(zones[i], x, y)) {
			setMessage("Point is inside zone : " + JSON.stringify(zoneIds[i]));
			return true;
		}
		return false;
	}
}

function isPositionOK(x, y, item) {

	for (var i = 0; i < zones.length; i++) {
		if (Raphael.isPointInsidePath(zones[i], x, y)) {

			// pointId + " : " +JSON.stringify(zoneIds[i]);
			if (!item)
				return false;

			for (var j = 0; j < radarAxes.length; j++) {
				var axis = radarAxes[j];
				if (item[axis] != zoneIds[i][axis]) {
					console.log("axis" + axis + "item :" + item[axis]
							+ " polyg: " + zoneIds[i][axis]);
					return false;
				}
			}
			return true;
		}

	}
	return false;

}

function positionUpdateAttrs(x, y, item) {
	if (!item)
		return null;
	for (var i = 0; i < zones.length; i++) {
		if (Raphael.isPointInsidePath(zones[i], x, y)) {
			// console.log(xfield + " : " + zoneIds[i][xfield] + "" + rfield + "
			// : " + zoneIds[i][rfield]);

			if (zoneIds[i][xfield] == "hell") {
				item.excluced = true;
			} else {
				item.excluced = false;
			}
			item[xfield] = zoneIds[i][xfield];
			item[rfield] = zoneIds[i][rfield];
			item.x = x;
			item.y = y;

			return item;
		}
	}
	return null;
}

function roundPath(path) {
	for (var i = 0; i < path.length; i++) {
		for (var j = 1; j < path[i].length; j++) {
			path[i][j] = Math.round(path[i][j]);
		}
	}
	return path
}

function setItemsCoordinates(items) {
	// return;
	var width = document.getElementById("technologies").clientWidth;
	var height = document.getElementById("technologies").clientHeight;
	var maxRuns = 1000;

	for (var j = 0; j < items.length; j++) {
		var isOk = false;
		var ko = true;
		runs = 0;
		var item = items[j];
		if (item.x != item.y)
			continue;
		do {
			var randomX = Math.random() * width;
			var randomY = Math.random() * height;
			for (var i = 0; i < zones.length; i++) {
				if (Raphael.isPointInsidePath(zones[i], randomX, randomY)) {
					if (item[xfield] == zoneIds[i][xfield]
							&& item[rfield] == zoneIds[i][rfield]) {
						// console.log(xfield + " : " + zoneIds[i][xfield] + ""
						// + rfield + " : " + zoneIds[i][rfield]);
						item[xfield] = zoneIds[i][xfield];
						item[rfield] = zoneIds[i][rfield];
						// var centroid = getCentroid(zones[i])
						item.x = randomX;
						item.y = randomY;
						isOk = true;
						ko = false;
						break;
					}
				}
			}

			runs++;
		} while (!isOk && runs < maxRuns)
		if (ko) {
			item.x = j * 20;
			item.y = j * 20;
			var fieldJson = {
				x : item.x,
				y : item.y
			};
			proxy_updateItemFields(dbName, currentRadarCollectionName, {
				id : item.id
			}, fieldJson);
		}
	}

}

function setAxisLabelsRadial() {
	var labelX = svgRadar.append("text").text(xfield).attr("dy", ".55em").attr(
			"class", "radarAxisTitle").attr("fill", "#00f").attr("transform",
			"translate(" + 60 + "," + 270 + ") rotate(-70)").attr("font-size",
			18);

	var labelR = svgRadar.append("text").text(rfield).attr("dy", ".55em").attr(
			"class", "radarAxisTitle").attr("fill", "#00f").attr("transform",
			"translate(600,60) rotate (57)").attr("font-size", 18);

	var text1 = svgRadar.append("text").text(dbName).attr("dy", ".55em").attr(
			"class", "radarAxisTitle").attr("fill", "#00f")
	// .attr("transform", "translate(" + (Rwidth - 150) + "," + 20 + ")")
	.attr("transform", "translate(" + 10 + "," + 20 + ")")
			.attr("font-size", 28);

}

function setAxisLabelsCartesian() {
	var labelX = svgRadar.append("text").text(xfield).attr("dy", ".55em").attr(
			"class", "radarAxisTitle axisX").attr("fill", "#00f").attr("transform",
			"translate(" + 100 + "," + 20 + ") ")
	// .attr("font-size", 18)
	// .attr("font-style","bold");

	var labelR = svgRadar.append("text").text(rfield).attr("dy", ".55em").attr(
			"class", "radarAxisTitle axisR").attr("fill", "#00f").attr("transform",
			"translate(2,350) rotate (-90)")
	// .attr("font-size", 18)
	// .attr("font-style","bold");

	var text1 = svgRadar.append("text").text(dbName).attr("dy", ".55em").attr(
			"class", "radarAxisTitle").attr("fill", "#00f").attr("transform",
			"translate(" + 10 + "," + 20 + ")")
	// .attr("font-size", 28)
	// .attr("font-style","bold");

	/*
	 * var labelX = svgRadar.text(320, RBy + 10, xfield).attr({ "font-size" :
	 * 22, 'text-anchor' : 'start', "fill" : "#00f", "transform" : "r-0"
	 * 
	 * });
	 * 
	 * var labelR = svgRadar.text(40, 300, rfield).attr({ "font-size" : 22,
	 * 'text-anchor' : 'start', "fill" : "#00f", "transform" : "t-70,0r-90" });
	 * var labelT = svgRadar.text(Rwidth - 150, 20, dbName).attr({ "font-size" :
	 * 28, 'text-anchor' : 'start', "fill" : "#00f"
	 * 
	 * });
	 */
}
