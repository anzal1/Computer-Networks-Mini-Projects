// Client side C/C++ program to demonstrate Socket
// programming
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include<fcntl.h>
#define PORT 8081
#define SIZE 1024

// write data received from sockfd to a file
void write_file(int sockfd){
    int n;
    FILE *fp;
    char* filename = (char *)"recv2.txt";
    char buffer[SIZE] ;
    
    fp = fopen(filename, "w");
	printf("Client 2: receiving from socket %d\n", sockfd);
    while (1) {

        n = recv(sockfd, buffer, SIZE, 0);
        
        if (n <= 0){
			break;
			return;
        } 
        fprintf(fp, "%s", buffer);
        bzero(buffer, SIZE);
    }
    return;
}

int main(int argc, char const* argv[])
{
	int sock = 0, valread, client_fd;
	struct sockaddr_in serv_addr;

	FILE *fp;
    char *filename = (char *)"send.txt";

	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		printf("\n Socket creation error \n");
		return -1;
	}

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(PORT);

	// Convert IPv4 and IPv6 addresses from text to binary
	// form
	if (inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr)<= 0) {
		printf("\nInvalid address/ Address not supported \n");
		return -1;
	}

	if ((client_fd = connect(sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr))) < 0) {
		printf("\nConnection Failed \n");
		return -1;
	}

	// write content into file
	write_file(sock);

	printf("Client 2: File received.\n");

	//closing the connected socket
	close(client_fd);
	return 0;
}
