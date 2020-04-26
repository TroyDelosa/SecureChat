# SecureChat
## Overview
SecureChat is a Java based network client that allows two users to securely communicate with one and another. It can also be used to chat to multiple individual users simultaneously. It uses both asymmetric and symmetric key encryption for the secure transfer of messages. Asymmetric key encryption is used for the secure transfer of a secret key, while symmetric key encryption is used for the secure transfer of messages.

## Classes
The application consists of 7 classes including the two 2 nested classes MessageListener and ConnectionListener. This is depicted in the following class diagram, excluding the MenuFrame and ChatFrame classes which only contain the GUI components of the application.

## Network Structure Design
Each peer on the network can act as both a client and a server. This means that they are capable of both requesting other peers to establish a connection and listening and accepting incoming connection requests.

## Encryption
When a client connects to a server, a key exchange takes place. The following steps show the process of the secure key exchange:
1.	The client generates an asymmetric key pair and server generates a secret key
2.	The client sends its public key to the server
3.	Server encrypts its secret key with the clients public key and sends it to the client
4.	The client decrypts the received secret key using its private key

## Strengths and weaknesses of implementation
Currently the application utilised both symmetric and asymmetric encryption. Asymmetric encryption is used to perform a secure transfer of the secret key, while the secret key is used to encrypt all sent messages to ensure their confidentiality. The main advantage of using symmetric encryption for sending data is that it is quicker and more efficient than asymmetric encryption.
Limitations and Vulnerabilities

## Limitations and Vulnerabilities
Currently there is no way of rejecting connection requests before the connection is established. Additionally, there is no way of verifying the actual identity of a connecting user. This is vulnerability as a malicious user could masquerade as another user by simply changing their name and IP address.

Another issue is that currently the message authentication code is generated using the same secret key that is used for encrypting messages. While this does ensure that any changes to the sent encrypted message will not go unnoticed, if a malicious user were to obtain the secret key though some form of cryptography, they would then be able to regenerate a valid message authentication code for any message they intercepted and modified. 
