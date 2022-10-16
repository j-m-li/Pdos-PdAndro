#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/mman.h>

int main(int argc, char *argv[])
{
    int n = 0;
    FILE *fp = stdin;
    int fd = STDIN_FILENO;

    //mprotect(buf, sizeof(buf), PROT_EXEC|PROT_READ|PROT_WRITE);
    if (argc > 1) {
        printf("%s\n", argv[1]);
    } else {
        printf("PDOS should be here...\n");
    }
    if (fd < 0) {
        perror("Connot open stdin");
    }

    fcntl(fd, F_SETFL, fcntl(fd, F_GETFL) | O_NONBLOCK);
    while (1) {
        char buf[1024];
        ssize_t l;

        l = read(fd, buf, sizeof(buf));
        if (l > 0) {
            write(STDOUT_FILENO, buf, l);
        } else {
            if (n > 50) {
                //write(1, "&gt;", 4);
                n = 0;
            }
            usleep(50000);
        }
        n++;
    }
    return 0;
}
