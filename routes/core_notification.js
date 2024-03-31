var express = require('express');
var router = express.Router();
var randomstring = require('randomstring');
var dotenv = require('dotenv');
dotenv.config();

// MySQL Connection
var mysql = require('mysql');
var connection = mysql.createConnection({
    host: '128.199.23.207',
	user: "csi",
	password: "csi",
	database: 'csiApp2022'
});

connection.connect(function(err) {
    if (!err) {
        console.log('Connected to new proposals');
    } else {
        console.log("Not Connected to new proposals");
    }
});

// POST /createnotification
router.post('/createnotification', function(req, res) {
    const { nd_title, nd_body, nd_sender_id, nd_receiver_ids , nc_id} = req.body;

    // Check if nd_receiver_ids is an array
    if (!Array.isArray(nd_receiver_ids)) {
        res.status(400).json({ error: 'nd_receiver_ids must be an array' });
        return;
    }

    // Serialize the array of receiver IDs into a string format
    const receiverIdsString = JSON.stringify(nd_receiver_ids);

    // SQL query to insert notification details
    const sql = `INSERT INTO notification_details (nd_title, nd_body, dateOfCreation, isDelete, isDisable, dateOfDeletion, nd_sender_id, nd_receiver_ids , nc_id) VALUES (?, ?, CURRENT_TIMESTAMP, 0, 0, CURRENT_TIMESTAMP, ?, ? , ?)`;

    // Execute the query to insert notification details
    connection.query(sql, [nd_title, nd_body, nd_sender_id, receiverIdsString , nc_id], function(err, result) {
        if (err) {
            console.error('Error executing MySQL query: ' + err.message);
            res.status(500).json({ error: 'Failed to create notification' });
            return;
        }

        console.log('Notification created successfully');

        // Get the ID of the inserted notification
        const insertedNotificationId = result.insertId;

        // Insert a row in notification_audience for each receiver ID
        const insertAudienceSql = `INSERT INTO notification_audience (nd_id, core_id) VALUES (?, ?)`;

        nd_receiver_ids.forEach(receiverId => {
            connection.query(insertAudienceSql, [insertedNotificationId, receiverId], function(err, result) {
                if (err) {
                    console.error('Error executing MySQL query: ' + err.message);
                    // Handle error if needed
                }
            });
        });

        res.status(200).json({ message: 'Notification created successfully' });
    });
});

router.get('/fetchnotifications', function(req, res) {
    console.log("request body",req);
    const userId = req.query.user_id; // Assuming user_id is passed as a query parameter

    // SQL query to fetch notifications with profile picture URL
    const sql = `SELECT nd.nd_id, nd.nd_title, nd.nd_body, nd.nc_id, cd.core_profilepic_url AS url 
                 FROM csiApp2022.notification_details nd 
                 JOIN csiApp2022.notification_audience na ON nd.nd_id = na.nd_id 
                 JOIN csiApp2022.core_details cd ON nd.nd_sender_id = cd.core_id 
                 WHERE na.core_id = ?`;

    // Execute the query
    connection.query(sql, [userId], function(err, results) {
        if (err) {
            console.error('Error executing MySQL query: ' + err.message);
            res.status(500).json({ error: 'Failed to fetch notifications' });
            return;
        }

        // Map the results to the desired JSON format
        const notifications = results.map(notification => ({
            title: notification.nd_title,
            heading: notification.nd_body,
            url: notification.url,
            cat_id : notification.nc_id
        }));

        // Send the JSON response
        res.status(200).json(notifications);
    });
});


module.exports = router;