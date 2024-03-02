const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var MinutePath = path.join(__dirname, "..", "..", "views", "pages", "minutes.ejs");
        if (session.userid != null) {
            response.render(MinutePath, {role : session.userrole});
          } else {
            response.redirect('/');
          }
    
    },

    minuteall : (request, response) => {
        connection.query("SELECT * FROM core_minute_manager ORDER BY cmm_id DESC", function(error, results, fields) {
            if (error) {
                console.error('Query error:', error);
                return response.redirect("/error"); // Handle the error appropriately
            }
    
            if (results && results.length > 0) {
                response.json({
                    data: results
                });
            } else {
                response.redirect("/error");
            }
        });
    },
    

    minutesingle : (request, response) => {

        var id = request.query.id;
    
        console.log(id);
    
        var query = `SELECT * FROM core_minute_manager WHERE cmm_id = "${id}"`;
        
        connection.query(query, function(error, data){
    
            response.json(data[0]);
    
        })
    
    }
};
