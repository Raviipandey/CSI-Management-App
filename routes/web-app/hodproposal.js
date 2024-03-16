const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");
const cors = require('cors');


module.exports = {
    get: (request, response) => {
        var session = request.session;
        var ProposalPath = path.join(__dirname, "..", "..", "views", "pages", "hodproposal.ejs");
        console.log(ProposalPath);

        // if (session.userid != null) {
        //     response.render(ProposalPath, {role : session.userrole});
        // } else {
        //     response.redirect('/');
        // }
        if (request.session.userrole === 2) {
            response.render(ProposalPath, {role : session.userrole, rolename: session.rolename});
        } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }

    },
    hodfetchall: (request, response) => {

        connection.query("SELECT * FROM core_proposals_manager WHERE proposals_status IN ( 2,3, -3) ORDER BY cpm_id DESC;", function (error, results, fields) {
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
    hodfetchsingle: (request, response) => {

        var id = request.query.id;
        // console.log(id);
        var query = `SELECT * FROM core_proposals_manager WHERE proposals_status IN (2, 3, -3) and cpm_id = "${id}"`;

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
    
    },
    countApprovedProposals: (request, response) => {
        console.log("Received request for /countApprovedProposals");
    
        connection.query("SELECT COUNT(*) AS count FROM core_proposals_manager WHERE proposals_status = 3", function (error, results, fields) {
            if (error) {
                console.error("Error fetching count of approved proposals:", error);
                response.status(500).json({message: "Error fetching count of approved proposals", error: error});
                return;
            }
            console.log("Query executed successfully.");
            
            if (results.length > 0) {
                console.log("Count of approved proposals:", results[0].count);
                response.json({
                    count: results[0].count
                });
            } else {
                console.log("No results found. Returning count as 0.");
                response.json({
                    count: 0
                });
            }
        });
    },
    countRejectedProposals: (request, response) => {
        console.log("Received request for /countRejectedProposals");
    
        connection.query("SELECT COUNT(*) AS count FROM core_proposals_manager WHERE proposals_status = -3", function (error, results, fields) {
            if (error) {
                console.error("Error fetching count of approved proposals:", error);
                response.status(500).json({message: "Error fetching count of rejected proposals", error: error});
                return;
            }
            console.log("Query executed successfully.");
            
            if (results.length > 0) {
                console.log("Count of rejected proposals:", results[0].count);
                response.json({
                    count: results[0].count
                });
            } else {
                console.log("No results found. Returning count as 0.");
                response.json({
                    count: 0
                });
            }
        });
    },
    countApprovedSBCProposals: (request, response) => {
        console.log("Received request for /countApprovedSBCProposals");
    
        connection.query("SELECT COUNT(*) AS count FROM core_proposals_manager WHERE proposals_status = 2", function (error, results, fields) {
            if (error) {
                console.error("Error fetching count of approved SBC proposals:", error);
                response.status(500).json({message: "Error fetching count of approved proposals", error: error});
                return;
            }
            console.log("Query executed successfully.");
            
            if (results.length > 0) {
                console.log("Count of approved SBC proposals:", results[0].count);
                response.json({
                    count: results[0].count
                });
            } else {
                console.log("No results found. Returning count as 0.");
                response.json({
                    count: 0
                });
            }
        });
    },
    countRejectedSBCProposals: (request, response) => {
        console.log("Received request for /countRejectedSBCProposals");
    
        connection.query("SELECT COUNT(*) AS count FROM core_proposals_manager WHERE proposals_status = -2", function (error, results, fields) {
            if (error) {
                console.error("Error fetching count of rejected proposals:", error);
                response.status(500).json({message: "Error fetching count of rejected proposals", error: error});
                return;
            }
            console.log("Query executed successfully.");
            
            if (results.length > 0) {
                console.log("Count of rejected proposals:", results[0].count);
                response.json({
                    count: results[0].count
                });
            } else {
                console.log("No results found. Returning count as 0.");
                response.json({
                    count: 0
                });
            }
        });
    }
    

};