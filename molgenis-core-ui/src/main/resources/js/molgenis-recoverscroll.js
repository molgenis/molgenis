/**
Inspired on http://scripterlative.com?recoverscroll
*/

var RecoverScroll=
{
 timer:null, x:0, y:0, cookieId:"RecoverScroll", dataCode:0,

 //body.onLoad
 init:function()
 {
  var offsetData,sx=0,sy=0,then,dt = new Date(),now = dt.getTime();

  //establish if this is IE, FF or else
  if( document.documentElement )
   this.dataCode=3;
  else
   if( document.body && typeof document.body.scrollTop!='undefined' )
    this.dataCode=2;
   else
    if( typeof window.pageXOffset!='undefined' )
     this.dataCode=1;
  
  //register a handler that puts every scroll action into the cookie
  this.addToHandler(window, 'onscroll', function(){ RecoverScroll.reset() });

  //scrolls to the previous scroll position as recovered from the cookie (if any)
  if(window.location.hash == ""
     && (offsetData=this.readCookie(this.cookieId)) != ""
     && (offsetData=offsetData.split('|')).length == 4
     && !isNaN(sx = Number(offsetData[1])) && !isNaN(sy = Number(offsetData[3])))
   {
     window.scrollTo(sx, sy);
   }

  //get current scroll position
  this.record();
 },

 //reset the scroll position to current
 reset:function()
 {
  clearTimeout(this.timer);
  this.timer=setTimeout(function(){RecoverScroll.record();}, 50);
 },

 //record current scroll position into cookie
 record:function()
 {
  var cStr;

  this.getScrollData();

  document.cookie=this.cookieId+"="+'x|'+this.x+'|y|'+this.y;
 },

 //reads the cookie value
 readCookie:function(cookieName)
 {
  var cValue="";

  if(typeof document.cookie!='undefined')
   cValue=(cValue=document.cookie.match(new RegExp("(^|;|\\s)"+cookieName+'=([^;]+);?'))) ? cValue[2] : "";

  return cValue;
 },

 //loads the scroll data (based on IE,FF, else)
 getScrollData:function()
 {
  switch( this.dataCode )
  {
   case 3 : this.x = Math.max(document.documentElement.scrollLeft, document.body.scrollLeft);
            this.y = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
            break;

   case 2 : this.x=document.body.scrollLeft;
            this.y=document.body.scrollTop;
            break;

   case 1 : this.x = window.pageXOffset; this.y = window.pageYOffset; break;
  }
 },

 //add event handler
 addToHandler:function(obj, evt, func)
 {
  if(obj[evt])
  {
   obj[evt]=function(f,g)
   {
    return function()
    {
     f.apply(this,arguments);
     return g.apply(this,arguments);
    };
   }(func, obj[evt]);
  }
  else
   obj[evt]=func;
 }
}