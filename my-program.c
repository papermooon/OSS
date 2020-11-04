#include<stdio.h>
#include<string.h>
#include<ctype.h>

// char readingWord;
// int sign;
// int leng;
// int error;
// 

 char buff[2000];
  
int main(int argc,char *argv[]) {
//    int count;
//    printf("The command line has %d arguments :\n",argc-1);
//    for (count = 1; count < argc; ++count) {
//        printf("%d: %s\n",count,argv[count]);
//    }
	
	FILE *fp = NULL;
	fp = fopen(argv[1], "r");
	
	if(fp==NULL)
	{
		printf("找不到对应的文件地址：");
		printf("%s",argv[1]);
	}
		
	else
	{
		while(fgets(buff,sizeof(buff),(FILE*)fp))
		{
			printf("%s",buff); 
		} 
		
	}

	
    return 0;
}
