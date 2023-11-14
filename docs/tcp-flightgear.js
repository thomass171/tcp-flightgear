/**
 * JS part of tcp-flightgear.html
 */

var host = "https://yard.de/tcp-flightgear";

function addPanel(label, contentProvider, optionalElement) {
    //console.log("addPanel " + label);
    var headerid = "header" + getUniqueId();
    var newElement = '<div id="' + headerid + '" class="w3-bar w3-black">';
    newElement += '<div class="w3-bar-item">' + label + '</div>';
    if (optionalElement != null) {
        newElement += optionalElement;
    }
    newElement += '</div>';
    var id = "row" + getUniqueId();
    newElement += '<div id="' + id + '" class="w3-panel w3-hide w3-show"><div>' + contentProvider() + '</div></div>'
    $("#panellist").append(newElement);

}

function launchSceneryScene(vr,nearview,hud) {

    var args = new Map();
    addCommonArgs(args, "");
    args.set("enableVR",vr);
    args.set("enableHud",hud);
    args.set("enableNearView",nearview);
    launchScene("SceneryScene",args);
}

function launchRailingScene(vr,nearview,hud) {

    var args = new Map();
    addCommonArgs(args, "");
    args.set("enableVR",vr);
    args.set("enableHud",hud);
    args.set("enableNearView",nearview);
    launchScene("RailingScene",args);
}

/**
 * init for tcp-flightgear.html
 */
function init() {
    var url = new URL(window.location.href);
    console.log("url=" + url);
    var hostparam = url.searchParams.get("host");
    if (hostparam != null) {
        host = hostparam;
        $("#debuginfo").html("(host="+hostparam+")");
    }

    $("#inp_ctrlPanel").val("0,0,0,200,90,0");
    // With "ReferenceSpaceType" 'local' instead of 'local-floor' -0.1 is better than -0.9. 0.6 good for 1.80m player in maze
    // But for BasicTravelScene and VrScene 0 seem to be better. So better use a neutral value 0 here initially and let
    // application adjust it. And have a vector3 tuple now.
    $("#inp_offsetVR").val("0.0, 0.0, 0.0");
    // for some unknown reason traffic needs to be lowered
    $("#inp_tf_offsetVR").val("0.0, -1.0, 0.0");
    $("#inp_teamSize").val("1");

    $.get(host + "/version.html", function(responseText) {
        var s = responseText;
        var index = s.indexOf("</div>");
        if (index != -1) {
            s = s.substring(0,index);
        }
        index = s.indexOf("<div>");
        if (index != -1) {
            s = s.substring(index+5);
        }
        $("#versionInfo").html("Latest Build: " + s);
    });
}