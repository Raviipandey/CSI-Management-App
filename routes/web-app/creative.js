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
        console.log("This is the id", id);
        
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
    
    },

    fetchcreative : (request , response) =>{
        // console.log("Request recieved" , request);
        var id = request.query.id;
        var query = `SELECT * FROM (SELECT core_creative_manager.cpm_id,proposals_event_name,proposals_event_category,proposals_event_date,speaker, proposals_venue , proposals_reg_fee_csi ,proposals_reg_fee_noncsi ,proposals_prize , proposals_desc , proposals_creative_budget, proposals_publicity_budget, proposals_guest_budget , creative_url FROM core_proposals_manager,core_creative_manager WHERE core_proposals_manager.cpm_id=core_creative_manager.cpm_id) AS creative WHERE cpm_id="${id}"`;
        connection.query(query , function(err , data){
            console.log(data);
            response.json(data)
        })
    },



};
