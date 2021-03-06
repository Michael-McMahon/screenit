var player = null;
var CUED = 5, UNSTARTED =-1, ENDED = 0, PLAYING = 1, PAUSED = 2, BUFFERING = 3;
var listLimit = 1;
var Globals =
{
	rddSubm: []
	,height: 0
	,width: 0
	,cur_chan_req: null
	,cur_chan: null
	/* build uri for search type channels */
	,search_str : (function() {
		var one_day = 86400, date = new Date(), unixtime_ms = date.getTime(), unixtime = parseInt(unixtime_ms / 1000);
		// MM 7-12-2012 Added "&syntax=cloudsearch"; Android UI non responsive without this paramater
		// MM 12/25/2013 Just youtube links for now; "+site%3A%27vimeo.com%27" removed 
		// MM 3/09/2014 Added limit=...; 100 is the maximum number of links returned 
		return "search/.json?q=%28and+%28or+site%3A%27youtube.com%27+site%3A%27youtu.be%27%29+timestamp%3A"
				+ (unixtime - 5 * one_day)
				+ "..%29&restrict_sr=on&sort=top&syntax=cloudsearch&limit="+listLimit;
	})()
};

$().ready(function() {
	getInitValues();
    createPlayer();
});

//Get paramaters from app which will be available at init
function getInitValues()
{
	//Sets the first channel to be loaded
	Globals.cur_chan = rtv2go.getInitialChannel();
	//Sets how many videos to list
	listLimit = rtv2go.getListLimit();
	//Get size from app
	requestSize();
}

//Request the player size from the android interface
function requestSize()
{
	lHeight = rtv2go.getHeight();
	lWidth = rtv2go.getWidth();
	setPlayerSize(lHeight, lWidth);
}
/* PARSES VIDEO LIST */
function loadChannel(channel)
{
	Globals.cur_chan = channel;
	if(player === null)
	{
		return;
	}
		//Request videos list from reddit
		var feed = getFeedURI(channel);

		var last_req = Globals.cur_chan_req;
		
	if (last_req !== null) {
		last_req.abort();
	}
	
        	Globals.cur_chan_req = $.ajax({
            	url: "http://www.reddit.com"+feed,
	            dataType: "jsonp",
        	    jsonp: "jsonp",
		    cache: false,
	            success: function(data) {
                	Globals.rddSubm = []; //reddit submissions list  [http://www.reddit.com/dev/api#GET_api_info]
			var ddc = data.data.children;
        	        for(var x in ddc){
               		     if(ddc[x].data.score > 1
                       		&& (ddc[x].data.domain === 'youtube.com' 
				|| ddc[x].data.domain === 'youtu.be')){
					//Reddit link pushed to list
                                	Globals.rddSubm.push(ddc[x].data);
	                    }
        	     	}

			if(Globals.rddSubm.length > 0){
				sendListToAndroid();
	        	}
			else{
		       		rtv2go.promptNoVideos(channel);
        		}
	            },
        	    error: function(jXHR, textStatus, errorThrown) {
                	if(textStatus !== 'abort'){
		            rtv2go.promptNoVideos(channel);
	                    rtv2go.toastMsgL('Could not load feed. Is reddit down?');
        	        }
	            },
		    complete: function(jXHR, textStatus)
		    {
		    }
	        });
}

function parseYoutubeID(url)
{
	ID = "";

        if(url.match(/(\?v\=|&v\=|&amp;v=)/)){
            parts = url.split('v=');
            ID = parts[1].substr(0,11);
        }else if(url.match(/youtu\.be/)){
            parts = url.split("/");
            ID = parts[3].substr(0,11);
        }

	return ID;
}	

function getYoutubeThumb(ID)
{
	return "http://i2.ytimg.com/vi/"+ID+"/default.jpg";
}

function getFeedURI(channel) {
	return "/r/"+channel+"/"
		+Globals.search_str
		+"&limit=100";
}

// Send a list of thumbnail urls to the app
function sendListToAndroid() {
	rtv2go.clearThumbnails();
	for ( var i in Globals.rddSubm) {
		// Get video title
		var this_video = Globals.rddSubm[i];
		if (!this_video.title_unesc) {
			this_video.title_unesc = $.unescapifyHTML(this_video.title);
			this_video.title_quot = String(this_video.title_unesc).replace(
					/\"/g, '&quot;');
		}

		rtv2go.addThumbnail( getYoutubeThumb( parseYoutubeID(Globals.rddSubm[i].url)),
				this_video.title_unesc);
	}
	rtv2go.thumbsLoaded();
}

function createPlayer()
{
	//configAndroidRes();
	loadYoutubeApi();	
}

//Called from webview after onSizeChanged
function setPlayerSize(h, w)
{
	// Pixel scaling factor performed by android webview
	dpr = window.devicePixelRatio;
	// Configure video size
	Globals.height = h / dpr;
	Globals.width = w / dpr;
	
	$("#yt_player").height(Globals.height);
	$("#yt_player").width(Globals.width);
	
	if(player)
	{
		player.setSize(Globals.height, Globals.width);
	}
}

// Ask android app how big the video should be
/*
function configAndroidRes() {
	// Pixel scaling factor performed by android webview
	dpr = window.devicePixelRatio;
	// Configure video size
	Globals.height = rtv2go.getHeight() / dpr;
	Globals.width = rtv2go.getWidth() / dpr;
	//$("#yt_player").height(Globals.height);
	//$("#yt_player").width(Globals.width);
}			
*/
function loadYoutubeApi()
{
	//This code loads the IFrame Player API code asynchronously.
    var tag = document.createElement('script');

    tag.src = "http://www.youtube.com/iframe_api";
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
}	

function onYouTubeIframeAPIReady() 
{
    var org = document.baseURI;
    new YT.Player('yt_player', 
	{ 
		height: Globals.height,
        width: Globals.width,
		events: {
			'onReady': onPlayerReady,
			'onError': ytError,
	    	'onStateChange': ytStateChange
        },
		playerVars: {
	    	//'origin': org,
		    'controls': 0,
			'iv_load_policy': 3,
			'modestbranding': 1,
			'rel': 0,
			'showinfo': 0,
			'autohide': 0
		}
    });
}

function onPlayerReady(event) {
	player = event.target;
	player.setSize(Globals.height, Globals.width);
	//Will load first channel
	loadChannel(Globals.cur_chan);
	//rtv2go.playerReady();
}

function loadVideo(i)
{
    cueVideo( parseYoutubeID(Globals.rddSubm[i].url));
}

function cueVideo(id)
{
	player.cueVideoById(id);
}

function playVideo()
{
	if(player == null)
	{
		return;
	}	
	switch(player.getPlayerState())
	{
	case PAUSED:
	case CUED:
		player.playVideo();
		rtv2go.toastMsgS("play");
		break;
	case PLAYING:
		player.pauseVideo();
		rtv2go.toastMsgS("pause");
		break;
	default:
		return;
	}

}
//https://developers.google.com/youtube/iframe_api_reference#onStateChange
function ytStateChange(event)
{
	//Autoplay
	if(event.data == CUED)
	{
		playVideo();
	}
}

//https://developers.google.com/youtube/iframe_api_reference#onError
var ERR_ID = 2, ERR_HTML5 = 5, ERR_DNE = 100, ERR_EMB01 = 101, ERR_EMB50 = 150;
function ytError(event)
{
	var errCD = event.data
	var errMsg;

	switch(errCD)
	{
		case ERR_ID:
		case ERR_DNE:
		      errMsg = "ERROR: Video was not found.";
		      break;
		case ERR_HTML5:
		case ERR_EMB01:
		case ERR_EMB50:
		      errMsg = "ERROR: Video can not be viewed in seenit." 
		      break;
	default:
		      errMsg = "Youtube playback responded with error: "+errCD;
	}

	rtv2go.toastMsgL(errMsg);
}
/*
function consoleLog(string) {
    rtv2go.androidLog(string);
}*/
