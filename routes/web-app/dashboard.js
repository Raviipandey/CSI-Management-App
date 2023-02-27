const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (req, res, next) => {

        var session = req.session;
        var DashboardPath = path.join(__dirname, "..", "..", "views", "dashboard.ejs");
        console.log(session.userrole);

        if (session.userid != null) {
            res.render(DashboardPath, {role : session.userrole});
        } else {
            res.redirect('/');
        }
    },
    pydata: (request, response) => {

        connection.query("SELECT proposals_three_track FROM core_proposals_manager ORDER BY cpm_id DESC", function (error, results, fields) {
            var proposals_three_track = '';
            var cat_academics = 0;
            var cat_wellness = 0;
            var cat_aspiration = 0;
            if (results.length > 0) {
                console.log(results);

                for (var count = 0; count < results.length; count++) {

                    if (results[count].proposals_three_track == 1) {
                        cat_academics = cat_academics + 1;
                    }
                    else if (results[count].proposals_three_track == 3) {
                        cat_wellness = cat_wellness + 1;
                    }
                    else if (results[count].proposals_three_track == 2) {
                        cat_aspiration = cat_aspiration + 1;
                    }

                }
                response.json({
                    data: {
                        acad: cat_academics,
                        well: cat_wellness,
                        aspi: cat_aspiration,
                    }

                });
                console.log("acad:" + cat_academics, "well:", cat_wellness, "aspi", cat_aspiration);
            }
            else {
                response.redirect("/error");
            }
            response.end();
        })

    },

    confirmall : (request, response) => {

        connection.query("SELECT * FROM core_proposals_manager where proposals_status = 3 ORDER BY cpm_id DESC",function(error,results,fields){
            if (results.length > 0) {
                    console.log(results);
                        response.json({
                data:results
            });
            } else {
                console.log("confirmed error",error)
                // response.redirect("/error");
            }
            response.end();
        })
    
    }


};
