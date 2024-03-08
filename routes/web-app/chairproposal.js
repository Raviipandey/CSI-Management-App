const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");
const cors = require('cors');


module.exports = {
    get: (request, response) => {
        var session = request.session;
        var ProposalPath = path.join(__dirname, "..", "..", "views", "pages", "chairproposal.ejs");
        console.log(ProposalPath);

        if (request.session.userrole === 3) {
            response.render(ProposalPath);
        } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }

    },
    chairfetchall: (request, response) => {

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
    chairfetchsingle: (request, response) => {

        var id = request.query.id;
        // console.log(id);
        var query = `SELECT * FROM core_proposals_manager where cpm_id = "${id}"`;

        connection.query(query, function (error, data) {

            response.json(data[0]);

        })

    },
    chairstatusconf: (request, response) => {

        var id = request.query.id;
        // var status = request.query.status;
    //    console.log("test: "+id);
        var query = `UPDATE core_proposals_manager 
        SET proposals_status = 1 
        WHERE cpm_id = "${id}"`;
        
        connection.query(query, function(error, data){
    
            response.json(data);
    
        })
    
    },
    chairstatusrej : (request, response) => {

        var id = request.query.id;
        // var status = request.query.status;
    //    console.log("test: "+id);
        var query = `UPDATE core_proposals_manager 
        SET proposals_status = -1
        WHERE cpm_id = "${id}"`;
        
        connection.query(query, function(error, data){
            console.log(data);
    
            response.json(data);
    
        })
    
    },
};