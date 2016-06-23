Gerrit.install(function(self) {
/*
   Gerrit.on('showchange', function (c) {
       alert("At least something");
   });
   Gerrit.onAction('change', 'reviewers', function (c) {
       alert("On reviewer added");
   });
*/

(function(open) {
   XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {
       this.url = url;
       open.call(this, method, url, async, user, pass);
   };
})(XMLHttpRequest.prototype.open);

(function(send) {
    XMLHttpRequest.prototype.send = function(data) {
        this.fakeReturn = false;
        if (data != null && data.search(/\{\"reviewer\":\".*\"\}/) != -1) {
            if (data.indexOf("Chewbacca") != -1) {
                if (confirm("The reviewer already has 5 reviews assigned; add him regardless?") != true) {
                    this.fakeReturn = true;
                    data = '';
                }
            } else if (data.indexOf("Darth Vader") != -1) {
                if (confirm("The reviewer has scheduled PTO in the next few days; add him regardless?") != true) {
                    this.fakeReturn = true;
                    data = '';
                }
            }
        }
        // in this case I'm injecting an access token (eg. accessToken) in the request headers before it gets sent
        //if(accessToken) this.setRequestHeader('x-access-token', accessToken);

        send.call(this, data);
    };

})(XMLHttpRequest.prototype.send);


(function() {
    // create XMLHttpRequest proxy object
    var oldXMLHttpRequest = XMLHttpRequest;

    // define constructor for my proxy object
    XMLHttpRequest = function() {
        var actual = new oldXMLHttpRequest();
        var self = this;

        this.onreadystatechange = null;

        // this is the actual handler on the real XMLHttpRequest object
        actual.onreadystatechange = function() {
            self.status = actual.status;
            if (this.readyState == 4) {
                // actual.responseText is the ajax result

                // add your own code here to read the real ajax result
                // from actual.responseText and then put whatever result you want
                // the caller to see in self.responseText
                // this next line of code is a dummy line to be replaced
                if (this.fakeReturn) {
                    self.status = 200;
                }
                //self.responseText = '{"msg": "Hello"}';
            }
            if (self.onreadystatechange) {
                return self.onreadystatechange();
            }
        };

        // add all proxy getters
        ["statusText", "responseType", "response",
         "readyState", "responseXML", "upload", "responseText"].forEach(function(item) {
            Object.defineProperty(self, item, {
                get: function() {return actual[item];}
            });
        });

        // add all proxy getters/setters
        ["ontimeout, timeout", "withCredentials", "onload", "onerror", "onprogress"].forEach(function(item) {
            Object.defineProperty(self, item, {
                get: function() {return actual[item];},
                set: function(val) {actual[item] = val;}
            });
        });

        // add all pure proxy pass-through methods
        ["addEventListener", "send", "open", "abort", "getAllResponseHeaders",
         "getResponseHeader", "overrideMimeType", "setRequestHeader"].forEach(function(item) {
            Object.defineProperty(self, item, {
                value: function() {return actual[item].apply(actual, arguments);}
            });
        });
    }
})();


/*
(function(send) {

    XMLHttpRequest.prototype.send = function(data) {

        if (data != null && data.search(/\{\"reviewer\":\".*\"\}/) != -1) {
            if (confirm("The reviewer is busy; add him regardless?") != true) {
                //return;
                data = '{"reviewer": "boo"}';
            }
        }
        // in this case I'm injecting an access token (eg. accessToken) in the request headers before it gets sent
        //if(accessToken) this.setRequestHeader('x-access-token', accessToken);

        send.call(this, data);
    };

})(XMLHttpRequest.prototype.send);

   (function(open) {
       XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {

                   var oldReady;
                   if (async) {
                       oldReady = this.onreadystatechange;
                       // override onReadyStateChange
                       this.onreadystatechange = function() {
                           if (this.readyState == 4) {
                               // this.responseText is the ajax result
                               // create a dummay ajax object so we can modify responseText
                               var self = this;
                               var dummy = {};
                               ["statusText", "status", "readyState", "responseType", "responseText"].forEach(function(item) {
                                   dummy[item] = self[item];
                               });
                               if (url.search(/changes\/\d+\/reviewers/) != -1) {
                                    dummy.status = 200;
                               }
                               if (oldReady) {
                                    return oldReady.call(dummy);
                               }
                           } else {
                               // call original onreadystatechange handler
                               if (oldReady) {
                                    return oldReady.call(arguments);
                               }
                           }
                       }
                   }

           if (method == "POST" && url.search(/changes\/\d+\/reviewers/) != -1) {
                url = Gerrit.url();
           }
           open.call(this, method, url, async, user, pass);
       };
   })(XMLHttpRequest.prototype.open);
*/
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

                    //alert("State change to 4");
                    this.status = 500;
               }
           }, false);
           open.call(this, method, url, async, user, pass);
       };
   })(XMLHttpRequest.prototype.open);
   */
});
