Gerrit.install(function(self) {
/*
   Gerrit.on('showchange', function (c) {
       alert("At least something");
   });
   Gerrit.onAction('change', 'reviewers', function (c) {
       alert("On reviewer added");
   });
*/

(function(send) {

    XMLHttpRequest.prototype.send = function(data) {


        // in this case I'm injecting an access token (eg. accessToken) in the request headers before it gets sent
        //if(accessToken) this.setRequestHeader('x-access-token', accessToken);

        send.call(this, data);
    };

})(XMLHttpRequest.prototype.send);
/*
   (function(open) {
       XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {
           if (url.search(/changes\/\d+\/reviewers/) != -1
                   && this.readyState == 4
                   && this.status == 200) {
               if (confirm("The reviewer is busy; add him regardless?") != true) {
                   return;
               }
           }
           open.call(this, method, url, async, user, pass);
       };
   })(XMLHttpRequest.prototype.open);
*/

/*
   (function(open) {
       XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {
           this.addEventListener("readystatechange", function() {
               if (url.search(/changes\/\d+\/reviewers/) != -1
                       && this.readyState == 4
                       && this.status == 200) {
                   if (confirm("The reviewer is busy; add him regardless?") != true) {
                       this.status = 500;
                       return;
                   }
               }
           }, false);
           open.call(this, method, url, async, user, pass);
       };
   })(XMLHttpRequest.prototype.open);
   */
});
