const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");
const cors = require('cors');


module.exports = {
    get: (request, response) => {
        var session = request.session;
        var ProposalPath = path.join(__dirname, "..", "..", "views", "pages", "sbcproposal.ejs");
        console.log(ProposalPath);

        if (request.session.userrole === 1) {
            response.render(ProposalPath);
        } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }

    },
    sbcfetchall: (request, response) => {

        connection.query("SELECT * FROM core_proposals_manager WHERE proposals_status IN (1, 2, -2) ORDER BY cpm_id DESC;", function (error, results, fields) {
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
    sbcfetchsingle: (request, response) => {

        var id = request.query.id;
        // console.log(id);
        var query = `SELECT * FROM core_proposals_manager WHERE proposals_status IN (1, 2, -2) and cpm_id = "${id}"`;

        connection.query(query, function (error, data) {

            response.json(data[0]);

        })

    },
    sbcstatusconf: (request, response) => {

        var id = request.query.id;
        // var status = request.query.status;
    //    console.log("test: "+id);
        var query = `UPDATE core_proposals_manager 
        SET proposals_status = 2
        WHERE cpm_id = "${id}"`;
        
        connection.query(query, function(error, data){
    
            response.json(data);
    
        })
    
    },
    sbcstatusrej : (request, response) => {

        var id = request.query.id;
        // var status = request.query.status;
    //    console.log("test: "+id);
        var query = `UPDATE core_proposals_manager 
        SET proposals_status = -2
        WHERE cpm_id = "${id}"`;
        
        connection.query(query, function(error, data){
            console.log(data);
    
            response.json(data);
    
        })
    
    },
};