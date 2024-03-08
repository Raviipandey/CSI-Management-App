const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");

module.exports = {
    get: (request, response) => {
        var session = request.session;
        var TechnicalPath = path.join(__dirname, "..", "..", "views", "pages", "technical.ejs");

        if (session.userid != null && (session.userrole == 1) && (session.userrole == 2) && (session.userrole == 3) && (session.userrole == 12)) {
            response.render(TechnicalPath, {role : session.userrole});
          } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }
    },

    techall : (request, response) => {
        const sql = `SELECT e.proposals_event_name, e.proposals_event_category, e.proposals_event_date, e.proposals_reg_fee_csi, e.proposals_reg_fee_noncsi, t.qs_set, t.software_install, tt.tasks, tt.status, tf.url AS file_url, tf.filename AS file_name FROM core_proposals_manager e INNER JOIN core_technical_manager t ON e.cpm_id = t.cpm_id LEFT JOIN (SELECT cpm_id, GROUP_CONCAT(task) AS tasks, GROUP_CONCAT(status) AS status FROM technical_tasks GROUP BY cpm_id) tt ON t.cpm_id = tt.cpm_id LEFT JOIN technical_files tf ON t.cpm_id = tf.eid;`;
    
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
