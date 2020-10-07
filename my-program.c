#include<stdio.h>
#include<string.h>
#include<ctype.h>
 char readingWord;
 int sign;
 int leng;
 int error;
 
 char token[100];
 int t_id;
 
 char buff[2000];
 
int com(char x[])
{
	if(strcmp(x,"BEGIN")==0)
	{
		printf("Begin\n");
		return 1;
	}
	if(strcmp(x,"END")==0)
	{
		printf("End\n");
		return 1;
	}
	if(strcmp(x,"FOR")==0)
	{
		printf("For\n");
		return 1;
	}
	if(strcmp(x,"IF")==0)
	{
		printf("If\n");
		return 1;
	}
	if(strcmp(x,"THEN")==0)
	{
		printf("Then\n");
		return 1;
	}
	
	return 0;
}

void change(char tt[],int last)
{
	int sum=0; 
	int j=0;
	while(j<last)
	{
		sum*=10;
		sum+=tt[j]-'0';
		j++;
	}
	
	printf("Int(%d)\n",sum);
	return ;
}
 
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
		printf("这是个空位置");
		printf("%s",argv[1]);
	}
		
	else
	{
		error=0;
		while(fgets(buff,sizeof(buff),(FILE*)fp))
		{
			sign=0;
			while(sign<strlen(buff))
			{
				while(buff[sign]==' ')
				sign++;
				
				readingWord=buff[sign];
					
					if(isalpha(readingWord))
					{
						memset(token,0,sizeof(token));
						t_id=0;
						
						while(isalpha(buff[sign])||isdigit(buff[sign]))
						{
							token[t_id]=buff[sign];
							t_id++;
							sign++;
						}	
						
						if(!com(token))
						{
							printf("Ident(%s)\n",token);
						}
					}
					
					else if(isdigit(readingWord))
					{
						memset(token,0,sizeof(token));
						t_id=0;
						
						while(isdigit(buff[sign]))
						{
							token[t_id]=buff[sign];
							t_id++;
							sign++;
						}
						
						change(token,t_id);
					}	
					
					else if(readingWord==':')
					{
						sign++;
						if(sign<strlen(buff)&&buff[sign]=='=')
						{
							printf("Assign\n");
							sign++; 
						} 
							
						else
							printf("Colon\n");
					} 
					else if(readingWord=='+') {printf("Plus\n");  sign++;}
					else if(readingWord=='*') {printf("Star\n"); sign++;}
					else if(readingWord==',') {printf("Comma\n"); sign++;}
					else if(readingWord=='(') {printf("LParenthesis\n"); sign++;}
					else if(readingWord==')') {printf("RParenthesis\n"); sign++;}
					else
					{
						sign++;
						if(readingWord!='\n'&&readingWord!='\r')
						{
							printf("Unknown\n");
							break;
						}
					}	
				
			}
		}
		
	}

	
    return 0;
}
