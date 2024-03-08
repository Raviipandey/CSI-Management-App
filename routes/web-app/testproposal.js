const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");
const cors = require('cors');


module.exports = {
    get: (request, response) => {
        var session = request.session;
        var ProposalPath = path.join(__dirname, "..", "..", "views", "pages", "testproposal.ejs");
        console.log(ProposalPath);

        if (request.session.userrole === 12) {
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

    }
};