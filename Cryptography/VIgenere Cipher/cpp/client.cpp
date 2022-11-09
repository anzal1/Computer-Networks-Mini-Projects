#include <iostream>
#include <string>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>
#include <sys/uio.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <fstream>
using namespace std;
// Client side

string generateKey(string str, string key)
{
    int x = str.size();

    for (int i = 0;; i++)
    {
        if (x == i)
            i = 0;
        if (key.size() == str.size())
            break;
        key.push_back(key[i]);
    }
    return key;
}

// This function returns the encrypted text
// generated with the help of the key
string cipherText(string str, string key)
{
    string cipher_text;

    for (int i = 0; i < str.size(); i++)
    {
        // converting in range 0-25
        char x = (str[i] + key[i]) % 26;

        // convert into alphabets(ASCII)
        x += 'A';

        cipher_text.push_back(x);
    }
    return cipher_text;
}

// This function decrypts the encrypted text
// and returns the original text

int main(int argc, char *argv[])
{
    // we need 2 things: ip address and port number, in that order
    if (argc != 3)
    {
        cerr << "Usage: ip_address port" << endl;
        exit(0);
    } // grab the IP address and port number
    char *serverIp = argv[1];
    int port = atoi(argv[2]);
    // create a message buffer
    char msg[1500];
    // setup a socket and connection tools
    struct hostent *host = gethostbyname(serverIp);
    sockaddr_in sendSockAddr;
    bzero((char *)&sendSockAddr, sizeof(sendSockAddr));
    sendSockAddr.sin_family = AF_INET;
    sendSockAddr.sin_addr.s_addr =
        inet_addr(inet_ntoa(*(struct in_addr *)*host->h_addr_list));
    sendSockAddr.sin_port = htons(port);
    int clientSd = socket(AF_INET, SOCK_STREAM, 0);
    // try to connect...
    int status = connect(clientSd,
                         (sockaddr *)&sendSockAddr, sizeof(sendSockAddr));
    if (status < 0)
    {
        cout << "Error connecting to socket!" << endl;
        return 0;
    }
    cout << "Connected to the server!" << endl;
    int bytesRead, bytesWritten = 0;
    struct timeval start1, end1;
    gettimeofday(&start1, NULL);
    while (1)
    {
        cout << "Enter key: ";
        string key;
        cin >> key;
        cin.ignore();
        cout << "Enter a message to send: ";
        string data;
        getline(cin, data);
        key = generateKey(data, key);
        // cin.ignore();
        data = cipherText(data, key) + '-' + key;
        memset(&msg, 0, sizeof(msg)); // clear the buffer
        strcpy(msg, data.c_str());
        if (data == "exit")
        {
            send(clientSd, (char *)&msg, strlen(msg), 0);
            break;
        }
        bytesWritten += send(clientSd, (char *)&msg, strlen(msg), 0);
        cout << "Awaiting server response..." << endl;
        memset(&msg, 0, sizeof(msg)); // clear the buffer
        bytesRead += recv(clientSd, (char *)&msg, sizeof(msg), 0);
        if (!strcmp(msg, "exit"))
        {
            cout << "Server has quit the session" << endl;
            break;
        }
        cout << "Server: " << msg << endl;
    }
    gettimeofday(&end1, NULL);
    close(clientSd);
    cout << "********Session********" << endl;
    cout << "Bytes written: " << bytesWritten << " Bytes read: " << bytesRead << endl;
    cout << "Elapsed time: " << (end1.tv_sec - start1.tv_sec)
         << " secs" << endl;
    cout << "Connection closed" << endl;
    return 0;
}