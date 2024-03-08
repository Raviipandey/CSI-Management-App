const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var PublicityPath = path.join(__dirname, "..", "..", "views", "pages", "publicity.ejs");
        if (session.userid != null && (session.userrole == 1) && (session.userrole == 2) && (session.userrole == 3) && (session.userrole == 12)) {
            response.render(PublicityPath, {role : session.userrole});
          } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }
    
    },

    publicityall : (request, response) => {
        const sql = `SELECT e.proposals_event_name, e.proposals_event_category, e.proposals_event_date,e.proposals_publicity_budget, p.pr_desk_publicity, p.pr_class_publicity, p.pr_member_count, p.pr_rcd_amount, p.pr_spent, (SELECT GROUP_CONCAT(pt.pub_tasks) FROM publicity_tasks pt WHERE pt.cpm_id = p.cpm_id) AS tasks,(SELECT GROUP_CONCAT(pt.status) FROM publicity_tasks pt WHERE pt.cpm_id = p.cpm_id) AS status,f.url AS documentUrl FROM core_proposals_manager e INNER JOIN core_pr_manager p ON e.cpm_id = p.cpm_id LEFT JOIN publicity_files f ON p.cpm_id = f.eid;`;
    
        connection.query(sql, function(error, results, fields) {
            if (error) {
                console.error('Query error:', error);
                response.sendStatus(500); // Internal Server Error
            } else {
                response.json({
                    data: results
                });
            }
        });
    }
};
