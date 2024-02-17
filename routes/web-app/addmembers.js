const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");
module.exports = {
    get: (request, response) => {
        var session = request.session;
        var FeedbackPath = path.join(__dirname, "..", "..", "views", "pages", "addmembers.ejs");
        if (session.userid != null) {
            response.render(FeedbackPath, { role: session.userrole });
        } else {
            response.redirect('/');
        }
    },

    addmembers: (request, response) => {
        console.log("Ye rahi request", request.body);
        const { role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name } = request.body;
        
        // Use a callback to handle the result or error
        connection.query(
            'INSERT INTO csiApp2022.core_details (core_role_id, core_en_fname, core_en_lname, core_rollno, core_mobileno, core_email, core_pwd, core_branch, core_class) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name],
            (error, result) => {
                if (error) {
                    console.error('Error inserting data into the database:', error.message);
                    response.status(500).send('Internal Server Error');
                } else {
                    console.log('Data inserted successfully!');
                    response.status(200).send('Data inserted successfully!');
                }
            }
        );
    }
};