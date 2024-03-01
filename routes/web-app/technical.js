const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var TechnicalPath = path.join(__dirname, "..", "..", "views", "pages", "technical.ejs");

        if (session.userid != null && (session.userrole == 1)) {
            response.render(TechnicalPath, {role : session.userrole});
          } else {
            response.redirect('/');
          }
    },

    techall : (request, response) => {

        connection.query("SELECT * FROM core_technical_manager ORDER BY cpm_id DESC",function(error,results,fields){
            if (results.length > 0) {
                    // console.log(results);
                        response.json({
                data:results
            });
            } else {
                response.redirect("/error");
            }
            response.end();
        })
    
    },
    techsingle : (request, response) => {
        connection.query("SELECT * FROM technical_files ORDER BY eid DESC",function(error,results,fields){
            if (results.length > 0) {
                    // console.log(results);
                        response.json({
                data:results
            });
            } else {
                response.redirect("/error");
            }
            response.end();
        })
    }
};
