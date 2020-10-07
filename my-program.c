#include<stdio.h>
#include<string.h>
 char readingWord;
 char token[100];
 char buff[2000];
 
int main(int argc,char *argv[]) {
    int count;
    printf("The command line has %d arguments :\n",argc-1);
    for (count = 1; count < argc; ++count) {
        printf("%d: %s\n",count,argv[count]);
    }
	
	FILE *fp = NULL;
	fp = fopen(argv[0], "rb");
	if(fp==NULL)
	printf("!!!!!!!!");
	else
	{
	fgets(buff,2000, (FILE*)fp);
	
	puts(buff);
	fgets(buff,2000, (FILE*)fp);
	
	puts(buff);
	fgets(buff,2000, (FILE*)fp);
	
	puts(buff);
	fgets(buff,2000, (FILE*)fp);
	
	puts(buff);
	}

	
    return 0;
}
