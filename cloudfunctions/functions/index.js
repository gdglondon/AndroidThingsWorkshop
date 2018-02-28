// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.readTemperature = functions.https.onRequest((req, res) => {
    return admin.database().ref('/home').once('value', (snapshot) => {
        let result = parseFloat(Math.round(snapshot.val().temperature * 100) / 100).toFixed(2);
        console.log("result", result);
        res.status(200).send('Your temperature at home is: ' + result + "\n");
    });
});

'use strict';

// const functions = require('firebase-functions'); // Cloud Functions for Firebase library
const DialogflowApp = require('actions-on-google').DialogflowApp; // Google Assistant helper library

exports.dialogflowFirebaseFulfillment = functions.https.onRequest((request, response) => {
    console.log('Dialogflow Request headers: ' + JSON.stringify(request.headers));
    console.log('Dialogflow Request body: ' + JSON.stringify(request.body));

    processRequest(request, response);
});

function processRequest(request, response) {
    let action = request.body.result.action; // https://dialogflow.com/docs/actions-and-parameters
    let parameters = request.body.result.parameters; // https://dialogflow.com/docs/actions-and-parameters
    let inputContexts = request.body.result.contexts; // https://dialogflow.com/docs/contexts
    let requestSource = (request.body.originalRequest) ? request.body.originalRequest.source : undefined;

    const googleAssistantRequest = 'google'; // Constant to identify Google Assistant requests
    const app = new DialogflowApp({request: request, response: response});

    console.log("Received action: ", action);
    if (action === 'input.temperature') {
        temperatureRead();
    } else if (action === 'input.light') {
        setLight();
    } else {
        unknownRequest()
    }

    function unknownRequest() {
        sendSimpleMessage('I\'m having trouble, can you try that again?');
    }

    function sendSimpleMessage(message) {
        if (requestSource === googleAssistantRequest) {
            sendGoogleResponse(message); // Send simple response to user
        } else {
            sendResponse(message); // Send simple response to user
        }
    }

    function setLight() {
        let state = parameters.state ? parameters.state : 'off';
        let value = state === 'on';
        writeToLightDb(value);
    }

    function writeToLightDb(value) {
        admin.database().ref('/home/light').set(value);
        sendSimpleMessage('Setting your light to ' + value);
    }

    // function setLight() {
    //     let state = parameters.state ? parameters.state : 'off';
    //     let value = state === 'on';
    //     writeToLightDb(value);
    // }
    //
    // function writeToLightDb(value) {
    //     return admin.database().ref('/home/light').set(value => {
    //         let message = 'Setting your light to ' + value;
    //         sendSimpleMessage(message);
    //     });
    // }

    function temperatureRead() {
        var responseText = 'Unable to read temperature';
        admin.database().ref('/home').once('value', (snapshot) => {

            let result = parseFloat(Math.round(snapshot.val().temperature * 100) / 100).toFixed(2);
            responseText = 'Your temperature at home is: ' + result + "\n";
            console.log(responseText);

            // Use the Actions on Google lib to respond to Google requests; for other requests use JSON
            if (requestSource === googleAssistantRequest) {
                let responseToUser = {
                    speech: responseText,
                    text: responseText
                };
                sendGoogleResponse(responseToUser);
            } else {
                let responseToUser = {
                    //data: richResponsesV1, // Optional, uncomment to enable
                    speech: responseText,
                    text: responseText

                };
                sendResponse(responseToUser);
            }

        });
    }

    function sendGoogleResponse(responseToUser) {
        if (typeof responseToUser === 'string') {
            app.ask(responseToUser); // Google Assistant response
        } else {
            // If speech or displayText is defined use it to respond
            let googleResponse = app.buildRichResponse().addSimpleResponse({
                speech: responseToUser.speech || responseToUser.displayText,
                displayText: responseToUser.displayText || responseToUser.speech
            });
            // Optional: Overwrite previous response with rich response
            if (responseToUser.googleRichResponse) {
                googleResponse = responseToUser.googleRichResponse;
            }
            // Optional: add contexts (https://dialogflow.com/docs/contexts)
            if (responseToUser.googleOutputContexts) {
                app.setContext(...responseToUser.googleOutputContexts);
            }

            console.log('Response to Dialogflow (AoG): ' + JSON.stringify(googleResponse));
            app.ask(googleResponse); // Send response to Dialogflow and Google Assistant
        }
    }

    // Function to send correctly formatted responses to Dialogflow which are then sent to the user
    function sendResponse(responseToUser) {
        // if the response is a string send it as a response to the user
        if (typeof responseToUser === 'string') {
            let responseJson = {};
            responseJson.speech = responseToUser; // spoken response
            responseJson.displayText = responseToUser; // displayed response
            responseJson.data = responseToUser;
            response.json(responseJson); // Send response to Dialogflow
        } else {
            // If the response to the user includes rich responses or contexts send them to Dialogflow
            let responseJson = {};
            // If speech or displayText is defined, use it to respond (if one isn't defined use the other's value)
            responseJson.speech = responseToUser.speech || responseToUser.displayText;
            responseJson.displayText = responseToUser.displayText || responseToUser.speech;
            // Optional: add rich messages for integrations (https://dialogflow.com/docs/rich-messages)
            responseJson.data = responseToUser.data;
            // Optional: add contexts (https://dialogflow.com/docs/contexts)
            responseJson.contextOut = responseToUser.outputContexts;

            console.log('Response to Dialogflow: ' + JSON.stringify(responseJson));
            response.json(responseJson); // Send response to Dialogflow
        }
    }
}