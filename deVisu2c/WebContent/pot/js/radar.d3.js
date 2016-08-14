var r = 6;
var w = 300;
var h = 25;
var point1 = null;
var point2 = null;
var fontSize = "12px";
var tiangleUpPath = "M0,6L-6,6L0,-6L6,6L0,6";
var tiangleDownPath = "M0,-6L-6,-6L0,6L6,-6L0,-6";
var rectPath = "M-6,-6L-6,6L6,6L6,-6L-6,-6";

var labelMaxSize = 20;

var x0, y0 = 0;
var svgRadar = null;
var svgRadarLegend = null;
var isDragging = false;

var hoverRect;
var hoverText;
var currentRadarNode = null;
var yLegendOffset = 0;
var hoverRect;
var hoverText;
var isResizing = false;
var resizeSquare = 20;
var minTextBoxSize=20;
var selection=[];

var dragDx;
var dragDy;
var oldX,oldY;





function drawRadarD3(data) {
	 var displayType=radarXmls[radarModelName].displayType
	hoverRect = svgRadar.append("rect").attr("x", 100).attr("y", 100).attr(
			"width", 100).attr("height", 20).attr("rx", 10).attr("ry", 10)
			.style("fill", "yellow").attr("visibility", "hidden");
	hoverText = svgRadar.append("text").attr("x", 100).attr("y", 100).attr(
			"dy", ".35em").text("ABBBBBA").attr("class", "textHover").style(
			"fill", "black").attr("visibility", "hidden");

	points = svgRadar.selectAll(".pointsRadar").data(data).enter().append(
			"svg:g").on("dblclick", dblclickRadar).on("click", clickRadar)
			.attr("class", "pointsRadar").attr("id", function(d) {
				return "P_" + d.id;
			}).attr("visibility", function(d, i) {
				if (d.excluded === true && !showExcludedRadarPoints)
					return "hidden";
				else
					return "visible";

			})
			
			points.each(function (d){
				 var aPoint =d3.select(this);
				 var shape;
				 var size = radarXmls[radarModelName].Xml_getRealValue("size","size", d.size);
					if (!size || !$.isNumeric(size))
						size = 8;
					d.size2 = size;
				 if( displayType && displayType=="textBox" ){
						if(!d.w)
							d.w=100;
						if(!d.h)
							d.h=20;
				 }else{
					 if(!d.w)
							d.w=size/2;
						if(!d.h)
							d.h=size/2;	
				 }
	
				 
					
				
					if( displayType && displayType=="textBox" ){
			
						 shape= aPoint.append('rect')
						 .attr("width", d.w).attr("height", d.h).attr("rx", 2).attr("ry", 2)
					 }
					else if(!d.shape || d.shape=="circle"){
					 shape=aPoint.append("circle").attr("cx", 0).attr("cy", 0).attr("r",size);
				 }
				 else if(d.shape=="triangleUp"){
					 shape= aPoint.append('path')
					    .attr('d', tiangleUpPath);
					   
				 }
				 else if(d.shape=="triangleDown"){
					 shape= aPoint.append('path')
					    .attr('d', triangleDown);
				 }
				 else if( d.shape=="square"){
					 shape= aPoint.append('rect')
					   .attr("x",- size/2).attr("y", - size/2).attr(
					"width", size).attr("height", size).attr("rx", 0).attr("ry", 0)
				 }
				 
				 else if( displayType="textBox" || d.shape=="textBox"){
					 shape= aPoint.append('rect')
					   .attr("x",- size/2).attr("y", - size/2).attr(
					"width", size).attr("height", size).attr("rx", 10).attr("ry", 10)
				 }
				 
				 shape.style(
							"stroke",
							function(d) {
								return "000";
								
							})

					.style(
							"fill",
							function(d) {
							/*
							 * return
							 * radarXmls[radarModelName].Xml_getRealValue("color",
							 * "color", d.color);
							 */
								
								return radarXmls[radarModelName].Xml_getRealValue("color",
										null, d.color);
							})
						.attr("class", "shape")
						.style("z-index", function(d){
						if(d.maturity=="layer")
							return 20;
						return 10;
						
					});
						
					;
			});
points.append("text").attr("x", function(d) {
		
		if(displayType=="textBox")
			return 5;
		return (d.size2 / 2) + 5;
	}).attr("dy", function(d){
		if(displayType=="textBox")
			return 12;
		return ".35em"})
	.text(function(d) {
		if (d.label > labelMaxSize)
			return d.label.substring(0, labelMaxSize) + "..."
		return d.label;
	}).style("fill", function(d){
		if(d.textColor) return d.textColor;
		return "black"})
		.style("font-weight", function(d){
		if(d.textBold) return "bold";
		return "normal"})
	// .style("font-size", "10px")
	.attr("class", "radarPointLabel");

	points.attr("transform", function(d) {
		// d.x=-d.x/2;d.y=-d.y/2;
		return "translate(" + d.x + "," + d.y + ")";
	});

	hoverRect = svgRadar.append("rect").attr("x", 100).attr("y", 100).attr(
			"width", 100).attr("height", 20).attr("rx", 10).attr("ry", 10)
			.style("fill", "FFF78C").attr("visibility", "hidden").style(
					"opacity", 1);
	hoverText = svgRadar.append("text").attr("x", 100).attr("y", 100).attr(
			"dy", ".35em").text("ABBBBBA").attr("class", "textHover").style(
			"fill", "black").attr("visibility", "hidden").style("font-weight",
			"bold");
	
	// **************************DragRect************************

	var dataRect = [ {
		x : 1,
		y : 1,
		w : 2,
		h : 2,

	} ];
	dragRect = svgRadar.selectAll().data(dataRect).enter().append("rect").attr("width", function(d) {
		return d.w;
	}).attr("height", function(d) {
		return d.h;
	}).attr("x", function(d) {
		return d.x;
	}).attr("y", function(d) {
		return d.y;
	}).style("z-index", 100).style("stroke", "black").style("fill", "transparent").attr("class", "dragRect");
	
	var str="";

	var dragPoints = d3.behavior
			.drag()
			.on("dragstart", function(d, x, y) {
				initZoneDrag(d);
				hoverHide()

			})
			.on("drag", function(d, sx, sa, sy, sz) {
				isDragging = true;
				if (isRadarReadOnly) {
					return;
				}
				if (isResizing) {

			
					// oldRect =d;
					var rect=dragRect.datum();
					if(rect){
					/*
					 * var evtX = d3.event.sourceEvent.layerX; var evtY =
					 * d3.event.sourceEvent.layerY; // console.log(evtX+" :
					 * "+evtY); var newWidth = evtX - rect.x; var newHeight =
					 * evtY - rect.y;
					 */
					var newWidth = round(d3.event.x-rect.x);
					var newHeight = round(d3.event.y-rect.y);
					if(newWidth>0 && newHeight>0){
						
					
					d3.select(".dragRect").datum().w=newWidth;
					d3.select(".dragRect").datum().h=newHeight;
					d3.select(".dragRect").attr("width", newWidth).attr("height", newHeight);
				
					}
				}
					

				} else {
					
				var x = round(d3.event.x-dragDx);
				var y = round(d3.event.y-dragDy);
			
				d3.select(this).datum().x=x;
				d3.select(this).datum().y=y;
				d3.select(this).attr("transform", function(d) {

					return "translate(" + x + "," + y + ")";
				});
				}

			})
			.on(
					"dragend",
					function(d, sx, sa, sy, sz) {
						var item=null;

						if (isRadarReadOnly) {
							return;
						}
						var newWidth = 1;
						var newHeight = 1;
						var coefX = 1;
						var coefY =1;
						
						var x ;
						var y ;
						var changePointOK=false;
						if (isResizing) {	
							x=d.x;
							y=d.y;
							var rect=dragRect.datum();
							if(rect){
									newWidth = parseInt(rect.w);
									newHeight = parseInt(rect.h);
									if(newWidth<minTextBoxSize)
										newWidth=minTextBoxSize;
									if(newHeight<minTextBoxSize)
										newHeight=minTextBoxSize;
									changePointOK=true;
									d3.select(".dragRect").datum().w=0;
									d3.select(".dragRect").datum().h=0;
									d3.select(".dragRect").datum().x=0;
									d3.select(".dragRect").datum().y=0;
									d3.select(".dragRect").attr("width", 0).attr("height", 0).attr("x", 0).attr("y", 0);
								
							
							}
							
							
						}
						else{
							
							x=d.x;// -dragDx;
							y=d.y;// -dragDy;
							
						

					
						
					
						
						if (positionControMode == "CONFINED") {
							if (isPositionOK(x, y, d)) {	
								changePointOK=true;
							} else {
								setMessage("<font color=red>Cannnot move this point here :"+ d[xfield]+ ","+ d[rfield]+ "</font>");

							}
						}
						else if (positionControMode == "CHANGE-ATTRS") {
							item = positionUpdateAttrs(x, y, d);
							if (item) {
								changePointOK=true;
							} else {
								setMessage("<font color=red>Cannnot move this point here :"+ "</font>");

							}
						} 
						}
						
						
							var fieldJson = {
								};
						
							
						// for(var i=0;i<selection.length;i++){
							// var shape=d3.select(selection[i]);
								var shape=d3.select(this);
								
								if(isResizing){
									changePointOK=true;
									shape.datum().w=newWidth;// le groupe
									shape.datum().h=newHeight;
									shape.attr("width",newWidth).attr("height",newHeight);
									
									shape=d3.select(this).selectAll(".shape");// le
																				// rectangle
									shape.attr("width",newWidth).attr("height",newHeight);
									
									fieldJson.w=newWidth
									fieldJson.h=newHeight;
								}else{
									if(!changePointOK){
										x=oldX;
										y=oldY;
									}
									
									shape.datum().x=x;
									shape.datum().y=y;
									fieldJson.x=x
									fieldJson.y=y;
									shape.attr("x",x).attr("y",y);
									shape.attr("transform", function(d) {return "translate(" + x + "," + y + ")";});
								}
						if(item){//point changé de quadarant et  OK
							 fieldJson[xfield] = item[xfield];
								fieldJson[rfield] = item[rfield];
								fieldJson.excluded = item.excluced;
						}
						if(changePointOK){
						proxy_updateItemFields(dbName, collectionName,
								{
									id : d.id
								}, fieldJson);
						setMessage("<font color=green>move saved</font>");
						
						}
						
						
						$(radarDiv).css('cursor', 'default');
						isDragging = false;
						isResizing = false;
						d3.select(".dragRect").style("visibility","hidden");
						selection=[];
					});

	d3.selectAll(".pointsRadar").call(dragPoints);
	points.on("mouseover", function(node) {
		if( displayType && displayType=="textBox" ){
			return;
		}
		overCircle(node);
		return true;
	}).on("mouseout", function(node) {
		if( displayType && displayType=="textBox" ){
			return;
		}
		outCircle(node);
		return true;
	});

	
}


























// Define the zoom function for the zoomable tree
function zoom() {
	svgRadar.attr("transform", "translate(" + d3.event.translate + ")scale("
			+ d3.event.scale + ")");
}
function overCircle(node) {
	
	hoverShow(node.x, node.y, node.name);

}

function outCircle(node) {
	hoverHide();
}

function hoverShow(x, y, text) {
	if (!text.length)
		return;
	hoverRect.attr("x", x + 7);
	hoverRect.attr("y", y - 7);
	hoverRect.attr("width", 8 * text.length);
	hoverText.attr("x", x + 12);
	hoverText.attr("y", y + 3);
	hoverText.text(text);
	hoverRect.attr("visibility", "visible");
	hoverText.attr("visibility", "visible");
}

function hoverHide() {
	// return;
	hoverRect.attr("visibility", "hidden");
	hoverText.attr("visibility", "hidden");
}

function popupShow(x, y, text) {
	if (!text.length)
		return;
	// radar width: 1040;
	// radar height: 600;
	if (y > 180)
		y = 180;
	if (x > 540)
		x = 540;
	$("radarHoverPopup").draggable();
	$("radarHoverPopup").html(text);
	$("radarHoverPopup").css("visibility", "visible");
	$("radarHoverPopup").css("top", "" + (y - 10) + "px");
	$("radarHoverPopup").css("left", "" + (x + 5 - 230) + "px");

}

function popupHide() {
	// return;
	$("radarHoverPopup").css("visibility", "hidden");
	$("radarHoverPopup").css("visibility", "hidden");
}

function overPath(link) {
	var p = getMiddlePoint({
		x : link.source.x,
		y : link.source.y
	}, {
		x : link.target.x,
		y : link.target.y
	});
	hoverShow(p.x, p.y, "" + link.target.relType);

}

function clickBackground() {
	hoverHide();
}
function dblclickRadar(e) {
	// if (isDragging === false)
	// showRadarData(this.__data__);
	// getFormHTML(this.__data__);

}

function clickRadar(e, node) {
	hoverHide();
	var node = this.__data__;
	var e = d3.event;

	if (e.ctrlKey) {
		d3.select(this).selectAll(".shape").
		selection.push(this);// onCtlClickPointInRadar(node);
	} else if (e.altKey) {
		onAltClickPointInRadar(node);
	
	} else {
		onClickPointInRadar(node);
		
		

	}

}

function setRadarPointsVisbility() {
	d3.selectAll(".pointsRadar").attr("visibility", function(d, i) {

		var isVisible = true;
		if (d.visible === false)
			isVisible = false;
		if (d.excluded === true && !showExcludedRadarPoints)
			isVisible = false;
		if (isVisible)
			return "visible";
		else
			return "hidden";

	});
}

function drawLegendD3(data) {

	var xShape = 15;
	var xLabel = 35;
	yLegendOffset += 25;
	if (svgRadarLegend == null) {
		$("#legend").html("");
		// d3.select("svg").selectAll("*").remove();
		svgRadarLegend = d3.select("#legend").append("svg").attr("width", 200)
				.attr("height", 300);
	}
	/*
	 * points = svgRadarLegend.selectAll(".pointsLegend").data(data).enter()
	 * .append("svg:g").on("click", clickLegend).attr("class",
	 * "pointsLegend").attr("id", function(d) { return "P_" + d.label; });
	 */
	var currentType = "XXXXX";
	for (var i = 0; i < data.length; i++) {
		var d = data[i];
		if (currentType != d.type) {// draw type
			currentType = d.type;
			svgRadarLegend.append("text").attr("x", function(d) {
				return 18;
			}).attr("dy", ".35em").text(d.type).attr("transform",
					"translate(" + xLabel + "," + yLegendOffset + ")").attr(
					"class", "legendType");
			yLegendOffset += 20;
		}

		svgRadarLegend.append("circle").attr("cx", xShape).attr("cy",
				yLegendOffset).attr("r", function() {
			if (d.size)
				return d.size + "px";
			return 8;
		}).style("fill", function() {
			if (d.color)
				return d.color;
			return "eee";
		}).style("stroke", "bbb");

		svgRadarLegend.append("text").attr("x", xLabel)
				.attr("y", yLegendOffset).text(function() {
					var str = "";
					str += d.label;
					return str;
				}).style("fill", "black").attr("class", "radarPointLabel");

		yLegendOffset += 20;

	}
	;

}

function drawLegendD3Old(data) {
	// console.log(JSON.stringify(data));
	var x = 15;

	// var points =
	// svgRadar.selectAll("g").data(data).enter().append("svg:g").on("click",
	// click);
	points = svgRadarLegend.selectAll(".pointsLegend").data(data).enter()
			.append("svg:g").on("click", clickLegend).attr("class",
					"pointsLegend").attr("id", function(d) {
				return "P_" + d.label;
			});

	points.append("circle").attr("cx", 0).attr("cy", 0)
	/*
	 * .attr("stroke", "007") .attr("stroke-width", 1)
	 */
	.attr("r", function(d) {

		var size = d.size;
		if (size)
			size = d.size.value
		else
			size = 10;
		// y += size + 10;
		return size;
	})

	.style("fill", function(d) {
		var color = d.color;
		if (color)
			return color.value;
		return "eee";
	}).style("stroke", "bbb");

	points.append("text").attr("x", function(d) {
		return 18;
	}).attr("dy", ".35em").text(function(d) {
		var str = "";
		str += d.label.value;

		return str;
	}).style("fill", "black").attr("class", "radarPointLabel"); // .style("font-size",
	// "10px");

	points.attr("transform", function(d) {
		// d.x=-d.x/2;d.y=-d.y/2;
		yLegendOffset += 20;
		return "translate(" + x + "," + yLegendOffset + ")";
	});

}
function drawLegendType(type) {
	var x = 5;
	yLegendOffset += 20;
	if (svgRadarLegend == null) {

		$("#legend").html("");
		// d3.select("svg").selectAll("*").remove();
		svgRadarLegend = d3.select("#legend").append("svg").attr("width", 200)
				.attr("height", 300);
	}

	svgRadarLegend.append("text").attr("x", function(d) {
		return 18;
	}).attr("dy", ".35em").text(type).attr("transform",
			"translate(" + x + "," + yLegendOffset + ")").attr("class",
			"legendType");

}

function clickLegend(e) {
	var id = this.__data__.enumId.value;
	var currentEnum = radarXmls[radarModelName].Xml_getEnumeration(id);
	setMessage("legendId :" + currentEnum.label + " : " + !currentEnum.checked);
	showRightFilterDialog(id);

}

function forcePointColor(nodeIds, color_) {
	var colorField=radarXmls[radarModelName].XML_getFieldForRole("color");
	svgRadar.selectAll(".pointsRadar")
	.each(function(d) {
			if(false){
					d3.select(this).select(".shape").style("fill",function(d) {
						if ($.inArray(d.id,nodeIds)>-1) {
							return color_;
						}
						else{
						var color = "eef";
						if (d.color) {
							color = radarXmls[radarModelName]
									.Xml_getRealValue(
											"color",
											null,
											d[colorField]);
						}
						return color;
						}
					});
			}
					
					d3.select(this).select(".shape").style("stroke",function(d) {
						if ($.inArray(d.id,nodeIds)>-1) {
							return color_;
						}
						else{
						
							return "black";
						}
					})
					.style("stroke-width",function(d) {
						if ($.inArray(d.id,nodeIds)>-1) {
							return "2px";
						}
						else{
						
							return "1px";
						}
					});
					d3.select(this).style("opacity",function(d) {
						if ($.inArray(d.id,nodeIds)>-1) {
							return 1;
						}
						else if(d.textBold)
							return .8;
						else{
						
							return 0.2;
						}
					});
				
	});
				

}
function resetAllPointsOpacity(opacity){
	d3.selectAll(".pointsRadar").style("opacity",function(d) {
		return opacity;
	});
	
}

function updateRadarPoint(node) {
	var sizeField = radarXmls[radarModelName].XML_getFieldForRole("size");
	var colorField=radarXmls[radarModelName].XML_getFieldForRole("color");

	
	var points = svgRadar
			.selectAll(".pointsRadar")
			.each(function(d) {
						if (d.id == node.id || d.id == node) {
						

							d3.select(this)
									.select(".shape")
									.attr(
											"r",
											function(d) {
												var size = 8;
												if (d.size) {
													if (sizeField) {
														size = radarXmls[radarModelName]
																.Xml_getRealValue(
																		"size",
																		null,
																		node[sizeField]);
													}
												}
												return size;
											})
									.style(
											"fill",
											function(d) {
												var color = "eef";
												if (d.color) {
													color = radarXmls[radarModelName]
															.Xml_getRealValue(
																	"color",
																	null,
																	node[colorField]);
												}
												return color;
											});
						}

					});

}

function getRadarImg() {
	var html = d3.select("svg").attr("version", 1.1).attr("xmlns",
			"http://www.w3.org/2000/svg").node().parentNode.innerHTML;
	d3.select("testIMG").html(html);

	// injection des styles (Ã  revoir pas propre !!!)
	var style = ".radarPointLabel {fill: fff;font: Consolas, verdana, sans-serif;font-size: 12px;font-weight: normal;pointer-events: none;}";
	style += ".radarAxisTitle {font-size: 28, text-anchor: start, fill: 00f}";
	style += ".title {position: relative;top: 5px;left: 10px;font-size: 18px;font-family: serif;font-weight: bold;}";
	var styleDef = '<defs><style type="text/css"><![CDATA[' + style
			+ ']]></style></defs>';
	var p = html.indexOf(">");
	html = html.substring(0, p + 1) + styleDef + html.substring(p);
	return html;
	var imgSrc = 'data:image/svg+xml;base64,' + btoa(html);
	return imgSrc;

}


// *****************resize,

function initZoneDrag(zone) {
	var evtX = d3.event.sourceEvent.layerX;
	var evtY = d3.event.sourceEvent.layerY;
	dragDx=evtX-zone.x;
	dragDy=evtY-zone.y;
	oldX=zone.x;
	oldY=zone.y;	
	hoverHide();
	isResizing=false;
	 console.log("-------Resizing init "+zone.x+ "  : " + zone.y);
	 var displayType=radarXmls[radarModelName].displayType
		if( displayType && displayType=="textBox" ){
		
		
	
	var oldX2 = zone.x + zone.w;
	var oldY2 = zone.y + zone.h;
	// var evtX = d3.event.sourceEvent.offsetX;
	// var evtY = d3.event.sourceEvent.offsetY;
	
	
	
	d3.select(".dragRect").datum().w=zone.w;
	d3.select(".dragRect").datum().h=zone.h;
	d3.select(".dragRect").datum().x=zone.x;
	d3.select(".dragRect").datum().y=zone.y;
	
	d3.select(".dragRect").attr("x", zone.x).attr("y", zone.y);
	if (evtX > (oldX2 - resizeSquare) && evtY > (oldY2 - resizeSquare)) {
		isResizing = true;
		$(radarDiv).css('cursor', 'default');	
		d3.select(".dragRect").style("visibility","visible");
	
	} else {
		isResizing = false;
		$(radarDiv).css('cursor', 'default');
		d3.select(".dragRect").style("visibility","hidden");
	}
		}

}

function round(value){
	return Math.round(value/5)*5;
}

function getSVG(collectionName, id){
	var svg=$("#radarDiv").html(); 
// var svg1=d3.select("svg").html();
// var svg2=d3.selectAll(svgRadar).html();
// svg='<svg xmlns="http://www.w3.org/2000/svg" width="860"
// height="560">'+svg+'</svg>';
	svg=svg.replace(/&nbsp;/,"");
	svg=svg.replace(/fill: transparent;/,"");
	return svg;
}

function setRadarLabel(label,className){
	var xx=d3.selectAll("."+className).text(label);	
	
}
