const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var ProposalPath = path.join(__dirname, "..", "..", "views", "pages", "proposal.ejs");
        console.log(ProposalPath);

        if (session.userid != null) {
            response.render(ProposalPath, {role : session.userrole});
        } else {
            response.redirect('/');
        }

    },
    fetchall: (request, response) => {

        connection.query("SELECT * FROM core_proposals_manager ORDER BY cpm_id DESC", function (error, results, fields) {
            if (results.length > 0) {
                // console.log(results);
                response.json({
                    data: results
                });
            } else {
                response.redirect("/error");
            }
            response.end();
        })

    },
    fetchsingle: (request, response) => {

        var id = request.query.id;
        // console.log(id);
        var query = `SELECT * FROM core_proposals_manager WHERE cpm_id = "${id}"`;

        connection.query(query, function (error, data) {

            response.json(data[0]);

        })

    },
    hodstatusconf: (request, response) => {

        var id = request.query.id;
        // var status = request.query.status;
    //    console.log("test: "+id);
        var query = `UPDATE core_proposals_manager 
        SET proposals_status = 3 
        WHERE cpm_id = "${id}"`;
        
        connection.query(query, function(error, data){
    
            response.json(data);
    
        })
    
    },
    hodstatusrej : (request, response) => {

        var id = request.query.id;
        // var status = request.query.status;
    //    console.log("test: "+id);
        var query = `UPDATE core_proposals_manager 
        SET proposals_status = -3
        WHERE cpm_id = "${id}"`;
        
        connection.query(query, function(error, data){
            console.log(data);
    
            response.json(data);
    
        })
    
    }

};