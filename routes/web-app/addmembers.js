const connection = require("../../Database/dbConnect");
const session = require("express-session");
const path = require("path");
const { request } = require("http");
const { response } = require("express");
const multer = require('multer');
const csv = require('fast-csv');
const fs = require('fs');

const upload = multer({ dest: 'server_uploads/csv_upload' });
module.exports = {
    get: (request, response) => {
        var session = request.session;
        var FeedbackPath = path.join(__dirname, "..", "..", "views", "pages", "addmembers.ejs");
        if (session.userid != null && (session.userrole == 1)) {
            response.render(FeedbackPath, { role: session.userrole });
        } else {
            // Redirect the user or send an error message if they don't have the right role
            // res.status(403).send('Access Denied: You do not have permission to view this page.');
            response.redirect('/error?message=access-denied');
        }
    },
    addmembers: (request, response) => {
        console.log("Received request", request.body);
        const { role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, mem_strt, mem_end, acad } = request.body;
    
        // Step 1: Insert data into core_details to get core_id
        connection.query(
            'INSERT INTO csiApp2022.core_details (core_role_id, core_en_fname, core_en_lname, core_rollno, core_mobileno, core_email, core_pwd, core_branch, core_class, core_acad_year, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, acad, request.body.gender],
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
    },
    fetchCoreMembers: (request, response) => {
        const year = request.params.year; // Assuming you have defined ":year" as a route parameter
        // Validate the year format to prevent SQL injection and ensure it's a valid year format
        if (!/^\d{2}-\d{2}$/.test(year)) {
            console.error("Invalid year format received", year);
            response.status(400).json({message: "Invalid year format"});
            return;
        }
    
        console.log(`Received request for /members/${year}`);
    
        connection.query("SELECT cd.core_en_fname, crm.role_name FROM core_details AS cd JOIN core_role_master AS crm ON cd.core_role_id = crm.role_id WHERE core_acad_year = ?", [year], function (error, results, fields) {
            if (error) {
                console.error(`Error fetching core members for ${year}`, error);
                response.status(500).json({message: `Error fetching core members for ${year}`, error: error});
                return;
            }
            console.log(`Query executed successfully for ${year}.`);
    
            if (results.length > 0) {
                console.log(`Members fetched for ${year}:`, results.length);
                response.json({
                    year: year,
                    members: results.map(row => row.core_en_fname),
                    role : results.map(row=>row.role_name)
                });
            } else {
                console.log(`No results found for ${year}. Returning empty list.`);
                response.json({
                    year: year,
                    members: []
                });
            }
        });
    },
    uploadCSV: (request, response) => {
        upload.single('csvFile')(request, response, function (err) {
            if (err instanceof multer.MulterError) {
                return response.status(500).json(err);
            } else if (err) {
                return response.status(500).json(err);
            }
    
            const fileRows = [];
            fs.createReadStream(request.file.path)
                .pipe(csv.parse({ headers: true }))
                .on('error', error => console.error(error))
                .on('data', row => fileRows.push(row))
                .on('end', () => {
                    // Process the fileRows array
                    fileRows.forEach(row => {
                        const { role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, mem_strt, mem_end, acad, gender } = row;
                        
                        // Step 1: Insert data into core_details to get core_id
                        connection.query(
                            'INSERT INTO csiApp2022.core_details (core_role_id, core_en_fname, core_en_lname, core_rollno, core_mobileno, core_email, core_pwd, core_branch, core_class, core_acad_year, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                            [role, first_name, last_name, roll_no, mob_no, email, password, branch, class_name, acad, gender],
                            (error, result) => {
                                if (error) {
                                    console.error('Error inserting data into the core_details table:', error);
                                } else {
                                    // Step 2: Use the obtained core_id to insert data into core_membership
                                    const core_id = result.insertId;
                                    connection.query(
                                        'INSERT INTO csiApp2022.core_membership (core_id, membership_start_date, membership_end_date) VALUES (?, ?, ?)',
                                        [core_id, mem_strt, mem_end],
                                        (membershipError) => {
                                            if (membershipError) {
                                                console.error('Error inserting data into the core_membership table:', membershipError);
                                            }
                                        }
                                    );
                                }
                            }
                        );
                    });
    
                    fs.unlinkSync(request.file.path); // Optionally delete the file after processing
                    response.status(200).json({ success: true, message: 'CSV file has been processed successfully' });
                });
        });
    }
    
    
    
    
};