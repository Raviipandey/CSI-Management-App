const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var PublicityPath = path.join(__dirname, "..", "..", "views", "pages", "publicity.ejs");
        if (session.userid != null && (session.userrole == 1)) {
            response.render(PublicityPath, {role : session.userrole});
          } else {
            response.redirect('/');
          }
    
    },

    publicityall : (request, response) => {

            connection.query("SELECT * FROM core_pr_manager ORDER BY cpm_id DESC",function(error,results,fields){
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
