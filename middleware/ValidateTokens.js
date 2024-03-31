// MySQL Connection setup
var mysql = require('mysql');
const connection = mysql.createConnection({
  host: '128.199.23.207',
  user: 'csi',
  password: 'csi',
  database: 'csiApp2022'
});

// Ensure the database connection is successful
connection.connect(err => {
  if (err) {
    console.error('Error connecting to the database:', err);
    return;
  }
  console.log('Database connection established');
});

// Middleware to validate session tokens
function validateSessionToken(req, res, next) {
    console.log('Validating session token...');

    // Exclude validation for the root route ("/")
    if (req.originalUrl === '/' || req.originalUrl === '/logout' || req.originalUrl === '/.well-known/assetlinks.json' || req.originalUrl === '/login/newpassword') {
      console.log('Skipping token validation for root route');
      next(); // Continue to the next middleware or route handler
      return;
  }

    const authHeader = req.headers.authorization;

    // Check if the Authorization header is missing or doesn't start with 'Bearer '
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        console.log('No token provided or invalid format');
        return res.status(401).send({ error: 'No token provided or invalid format' });
    }

    // Extract the token from the Authorization header
    const sessionToken = authHeader.split(' ')[1];

    // Query the database to check if the session token is valid
    connection.query('SELECT core_id FROM core_details WHERE session_token = ?', [sessionToken], (err, result) => {
        if (err) {
            // Handle any database errors
            console.log('Database error', err);
            return res.status(500).send({ error: 'Database error' });
        } else if (result.length === 0) {
            // No user found with the given session token, indicating it's invalid or expired
            console.log('Invalid or expired session token');
            return res.status(401).send({ error: 'Invalid or expired session token' });
        }

        // At this point, the session token is valid
        console.log('Session token validated successfully for user_id:', result[0].core_id);

        // Optionally, attach user_id to the request object for use in subsequent middleware/route handlers
        req.core_id = result[0].core_id;

        // Continue processing the request
        next();
    });
}

module.exports = validateSessionToken;
