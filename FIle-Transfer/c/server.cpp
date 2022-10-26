/*
** server.cpp -> sends file to 5 clients
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include<iostream>
#include <fcntl.h> /* Added for the nonblocking socket */
using namespace std;

#define PORT "8081"   // port server is listening on
#define SIZE 1024

// Get sockaddr from sockaddr_in or sockaddr_in6 depending on whether it is IPv4 or IPv6
void *get_in_addr(struct sockaddr *sa)
{
	if (sa->sa_family == AF_INET) {
		return &(((struct sockaddr_in*)sa)->sin_addr);
	}

	return &(((struct sockaddr_in6*)sa)->sin6_addr);
}

void send_file( int sockfd){
    int n;
    char data[SIZE] = {0};

    FILE *fp;
    char* filename = (char *)"send.txt";
    
    fp = fopen(filename, "r");
 
  while(fgets(data, SIZE, fp) != NULL) {
    if (send(sockfd, data, sizeof(data), 0) == -1) {
      perror("Server: Error in sending file.");
      exit(1);
    }
    bzero(data, SIZE);
  }
}

int main(void)
{
    fd_set master;    // master file descriptor list, contains socket file descriptors we need to keep a track of
    fd_set read_fds;  // temp file descriptor list for select()
    fd_set write_fds;
    int fd_max;        // maximum file descriptor number

    int listener_sockfd;     // listen()er socket descriptor
    int newfd;        // newly accept()ed socket descriptor (client)

    struct sockaddr_storage remoteaddr; // client address
    socklen_t addrlen;

    char buf[256];    // buffer for client data
    int nbytes;

	char remoteIP[INET6_ADDRSTRLEN];

    int yes=1;        // for setsockopt() SO_REUSEADDR, below
    int i, j, rv;

	struct addrinfo hints, *ai, *p;

    FD_ZERO(&master);    // clear the master and temp sets
    FD_ZERO(&read_fds);

	// get a socket and bind it
	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM; // for TCP we use stream sockets
	hints.ai_flags = AI_PASSIVE; // we want to use the local addr of server and get a list of available addresses in ai

	if ((rv = getaddrinfo(NULL, PORT, &hints, &ai)) != 0) {
		fprintf(stderr, "Server: %s\n", gai_strerror(rv));
		exit(1);
	}
	
	for(p = ai; p != NULL; p = p->ai_next) {

        // create socket
    	listener_sockfd = socket(p->ai_family, p->ai_socktype, p->ai_protocol); 
		if (listener_sockfd < 0) { 
			continue;
		}
		
		// get rid of the "address already in use" error message
		setsockopt(listener_sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

        // bind socket to address
		if (bind(listener_sockfd, p->ai_addr, p->ai_addrlen) < 0) { 
			close(listener_sockfd);
			continue;
		}

		break;
	}

	// if we did not get bound
	if (p == NULL) {
		fprintf(stderr, "Server: Failed to bind\n");
		exit(2);
	}

	freeaddrinfo(ai); // done with this

    // listen on socket, BACKLOG = 5 so we allow 5 clients to wait in queue at a time
    if (listen(listener_sockfd, 5) == -1) {
        perror("listen");
        exit(3);
    }

    printf("Server: listening..\n");

    

    // add the listener_sockfd to the master set, since we want to monitor whether it can read
    FD_SET(listener_sockfd, &master);

    // keep track of the biggest file descriptor
    fd_max = listener_sockfd; // so far, it will be this

    // main loop
    for(;;) {
        read_fds = master; // copy master to temp
        write_fds = master;

        // select() to monitor which sockets are ready to read
        if (select(fd_max+1, &read_fds,&write_fds, NULL, NULL) == -1){
            perror("select");
            exit(4);
        }

        // run through the existing connections looking for data to read
        for(i = 0; i <= fd_max; i++) {

            if (FD_ISSET(i, &read_fds)) { // we got one
                if (i == listener_sockfd) { // server ready to read

                    // handle new connections
                    addrlen = sizeof remoteaddr;
					newfd = accept(listener_sockfd,(struct sockaddr *)&remoteaddr, &addrlen);

					if (newfd == -1) {
                        perror("accept");
                    } 
                    else{
                        
                        if (newfd > fd_max) {  // update max
                            fd_max = newfd;
                        }

                        FD_SET(newfd, &master); // add to master set

                        //fcntl(newfd, F_SETFL, O_NONBLOCK);

                        printf("Server: new connection from %s on socket %d\n",
                            inet_ntop(remoteaddr.ss_family, get_in_addr((struct sockaddr*)&remoteaddr), remoteIP, INET6_ADDRSTRLEN),
                            newfd);

                        int status = fcntl(newfd, F_SETFL, fcntl(newfd, F_GETFL, 0) | O_NONBLOCK);

                        if (status == -1){
                        perror("calling fcntl");
                        // handle the error.  By the way, I've never seen fcntl fail in this way
                        }                                              
                    }
                } 
                else {                
                } // END handle data from client
             }// END got new incoming connection
            if (FD_ISSET(i, &write_fds)) {

                // client ready to read
                    printf("Server: sending file to client %d\n", i);
                    send_file(i);
                    printf("Server: file sent\n");
                    close(i);
                                        
                    FD_CLR(i, &master);
            }
        } // END looping through file descriptors
    } // END for(;;)
    
    return 0;
}