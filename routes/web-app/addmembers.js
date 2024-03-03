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
        console.log("Received request", request.body);
        const { role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, mem_strt, mem_end, acad } = request.body;
    
        // Step 1: Insert data into core_details to get core_id
        connection.query(
            'INSERT INTO csiApp2022.core_details (core_role_id, core_en_fname, core_en_lname, core_rollno, core_mobileno, core_email, core_pwd, core_branch, core_class, core_acad_year) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, acad],
            (error, result) => {
                if (error) {
                    console.error('Error inserting data into the core_details table:', error.message);
                    response.status(500).json({ message: 'Internal Server Error', error: error.message });
                } else {
                    // Step 2: Use the obtained core_id to insert data into core_membership
                    const core_id = result.insertId; // Assuming the auto-incremented primary key is core_id
                    connection.query(
                        'INSERT INTO csiApp2022.core_membership (core_id, membership_start_date, membership_end_date) VALUES (?, ?, ?)',
                        [core_id, mem_strt, mem_end],
                        (membershipError, membershipResult) => {
                            if (membershipError) {
                                console.error('Error inserting data into the core_membership table:', membershipError.message);
                                response.status(500).json({ message: 'Internal Server Error', error: membershipError.message });
                            } else {
                                // Respond with success message
                                response.json({ message: 'Member added successfully', coreId: core_id });
                            }
                        }
                    );
                }
            }
        );
    },
    
    countMembers: (request, response) => {
        console.log("Received request for /countMembers");

        connection.query("SELECT COUNT(*) AS count FROM core_details", function (error, results, fields) {
            if (error) {
                console.error("Error fetching count of core", error);
                response.status(500).json({message: "Error fetching count of core", error: error});
                return;
            }
            console.log("Query executed successfully.");
            
            if (results.length > 0) {
                console.log("Count of core:", results[0].count);
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