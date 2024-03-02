const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var FeedbackPath = path.join(__dirname, "..", "..", "views", "pages", "creative.ejs");
        if (session.userid != null) {
            response.render(FeedbackPath, {role : session.userrole});
          } else {
            response.redirect('/');
          }
    
    },

    feedbackall : (request, response) => {

        connection.query("SELECT * FROM core_proposals_manager ORDER BY cpm_id DESC",function(error,results,fields){
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

    feedbacksingle : (request, response) => {

        var id = request.query.id;
        
    //    console.log("test: "+id);
       var query = `SELECT * FROM core_proposals_manager WHERE cpm_id = "${id}"`;
        // var query = `UPDATE events 
        // SET M_agenda = "${first_name}",  
        // WHERE eid = "${id}"`;
        
        connection.query(query, function(error, data){
            console.log(data[0]);
    
            response.json(data[0]);
    
        })
    
    }, 

    feedbackupdate : (request, response) => {

        var id = request.query.id;
        var updated_feedback = request.query.updated_feedback;
        //console.log("test: "+id+"//"+updated_feedback);
        var query = `UPDATE core_proposals_manager 
        SET proposals_comment = "${updated_feedback}"  
        WHERE cpm_id = "${id}"`;
        
        connection.query(query, function(error, data){
     
            response.json(data);
    
    
        });
    
    }


};
