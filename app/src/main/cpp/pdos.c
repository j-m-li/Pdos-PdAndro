#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <string.h>

int run(char *cmd)
{
    pid_t pid;
    int in;
    int out;
    int status = -1;
    char *args[10];
    args[0] = "sh";
    args[1] = "-c";
    args[2] = cmd;
    args[3] = NULL;

    pid = fork();
    in = dup(0);
    out = dup(1);
    if (pid == 0) {;
        execv("/bin/sh", args);
    } else {
        waitpid(pid, &status, 0);
        dup2(in, 0);
        dup2(out, 1);
   }

    return status;
}
int main(int argc, char *argv[])
{
    int n = 0;
    FILE *fp = stdin;
    int fd = STDIN_FILENO;
    char cmd[1024];

    //mprotect(buf, sizeof(buf), PROT_EXEC|PROT_READ|PROT_WRITE);
    if (argc > 1) {
        write(STDOUT_FILENO, argv[1], strlen(argv[1]));
    }
    write(STDOUT_FILENO, "\nprompt> ", 9);
    fcntl(fd, F_SETFL, fcntl(fd, F_GETFL) | O_NONBLOCK);
    while (1) {
        char buf[1024];
        ssize_t l;
        int i;
        l = read(fd, buf, sizeof(buf));
        if (l > 0) {
            write(STDOUT_FILENO, buf, l);
            i = 0;
            while (i < l) {
                if (buf[i] == '\n') {
                    cmd[n] = '\0';
                    n = 0;
                    run(cmd);
                    write(STDOUT_FILENO, "prompt> ", 7);
                    l = 0;
                } else {
                    if (n < (sizeof(cmd) - 2)) {
                        cmd[n] = buf[i];
                        n++;
                    } else {
                        n = 0;
                    }
                }
                i++;
            }
        } else {
            usleep(50000);
        }
    }
    return 0;
}
