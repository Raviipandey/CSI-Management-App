const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var FeedbackPath = path.join(__dirname, "..", "..", "views", "pages", "creative.ejs");
        if (session.userid != null ) {
            response.render(FeedbackPath, {role : session.userrole, rolename: session.rolename});
          } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }
    
    },

    // creativefetchall : (request, response) => {

    //     connection.query("SELECT * FROM core_proposals_manager where proposals_status = 3 ORDER BY cpm_id DESC;",function(error,results,fields){
    //         if (results.length > 0) {
    //                 // console.log(results);
    //                     response.json({
    //             data:results
    //         });
    //         } else {
    //             response.redirect("/error");
    //         }
    //         response.end();
    //     })
    
    // },

    creativefetchall: (request, response) => {
        // Adjust the query to also check for the existence of associated creative URLs
        let query = `SELECT cp.*, 
                            (SELECT COUNT(*) FROM core_creative_manager ccm WHERE ccm.cpm_id = cp.cpm_id AND (ccm.creative_heading LIKE '%.jpg' OR ccm.creative_heading LIKE '%.png' OR ccm.creative_heading LIKE '%.mp4' OR ccm.creative_heading LIKE '%.avi')) AS creative_count
                     FROM core_proposals_manager cp
                     WHERE cp.proposals_status = 3
                     ORDER BY cp.cpm_id DESC;`;
    
        connection.query(query, function(error, results, fields){
            if (error) {
                console.log(error);
                response.redirect("/error");
            } else if (results.length > 0) {
                // Modify each entry to include a flag indicating if creative URLs are available
                let modifiedResults = results.map(entry => ({
                    ...entry,
                    hasCreatives: entry.creative_count > 0
                }));
    
                response.json({
                    data: modifiedResults
                });
            } else {
                response.redirect("/error");
            }
            response.end();
        });
    },
    


    creativefetchsingle: (request, response) => {
        var id = request.query.id;
        var type = request.query.type; // Expect 'photo' or 'video'
    
        // Determine the file extension patterns based on the type
        var patterns = type === 'photo' ? ['%.jpg', '%.png'] : ['%.mp4', '%.avi'];
    
        // Correct the query to use parameterized values for better security
        // This approach also fixes the SQL syntax error by properly using the LIKE operator
        var query = "SELECT * FROM core_creative_manager WHERE cpm_id = ? AND (creative_heading LIKE ? OR creative_heading LIKE ?) ORDER BY cpm_id DESC;";
    
        // Execute the query with parameters
        connection.query(query, [id, ...patterns], function(err, data) {
            if (err) {
                console.error("Error fetching creative content", err);
                response.status(500).send("Internal Server Error");
            } else {
                response.json(data);
            }
        });
    }
    
    
    


};
