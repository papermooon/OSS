#include<stdio.h>
#include<string.h>
#include<ctype.h>

char stackIn[2000];
int s1,s2;
char stackOut[2000];
char buff[2000];

int martrix[6][6]={
	{1,-1,-1,-1,1,1},
	{1,1,-1,-1,1,1},
	{1,1,7,7,1,1},
	{-1,-1,-1,-1,0,7},
	{1,1,7,7,1,1},
	{-1,-1,-1,-1,7,7}
}; 
//1:> -1:< 7:Unknown 0:=

int ide(char z)
{
	if(z=='+')
		return 0;
	if(z=='*')
		return 1;
	if(z=='i')
		return 2;
	if(z=='(')
		return 3;
	if(z==')')
		return 4;
	if(z=='#')
		return 5;
	
	return -1;
}

char token[100];
char t0[]="i";
char t1[]="N+N";
char t2[]="(N)";
char t3[]="N*N";


int match(int x,int y)
{
	memset(token,0,sizeof(token));
	
	strncpy(token,stackIn+x,y-x+1);
	
	if(strcmp(token,t0)==0)
	{
		stackIn[y]='N';
		return 1;
	}
	
	if(strcmp(token,t1)==0)
	{
		s1=s1-2;
		return 1;
	}
	if(strcmp(token,t2)==0)
	{
		stackIn[s1-3]='N';
		s1=s1-2;
		return 1;
	}
	
	if(strcmp(token,t3)==0)
	{
		s1=s1-2;
		return 1;
	}
	
//	printf("%s",token);
	return 0;
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
		printf("找不到对应的文件地址：");
		printf("%s",argv[1]);
	}
		
	else
	{
		fgets(buff,sizeof(buff),(FILE*)fp);
		
			s1=0;
			s2=0;

			stackIn[s1]='#';
			s1++;

			strcpy(stackOut,buff);
			
			int h=0;
			for(;h<strlen(stackOut);h++)
			{
				if(stackOut[h]==10||stackOut[h]==13)
					break;
			}
			
			s2=h;
			stackOut[s2]='#';
			s2++;
			//s1s2指的地方永远是空的

			int step=0;
			int iner,outer;
			

			for(;step<s2;)
			{
				//先看看内部栈顶或者次级里是什么
				iner=ide(stackIn[s1-1]);
				if(iner==-1)
					iner=ide(stackIn[s1-2]);
				
				outer=ide(stackOut[step]);

				
				
				if(martrix[iner][outer]==7)
				{
					if(iner==5&&outer==5)
						return;
					printf("E\n");
					return;
				}
				else if(martrix[iner][outer]==-1||martrix[iner][outer]==0)//里面的小于等于外面的就移进
				{
					
					
					printf("I%c\n",stackOut[step]);
					stackIn[s1]=stackOut[step];
					s1++;
					step++;
					
				}     
				else
				{
					//找子串开始规约
					int sig=s1-1;
					if(ide(stackIn[s1-1])==-1) 
						sig--;
						
					int success=0;
					int j=sig-1;
					for(;j>=0;j--)
					{
						if(ide(stackIn[j])!=-1)
							if(martrix[ide(stackIn[j])][ide(stackIn[sig])]==0)
							{
								success=1;
								break;
							}
							if(martrix[ide(stackIn[j])][ide(stackIn[sig])]<0)
							{
								success=1;
								j++;
								break;
							}
					}
					
					if(!success)
						{
							printf("RE\n");
							return;
						}
					// j 到 sig 就是要规约的
					else
					{
						if(match(j,s1-1))
						{
							printf("R\n");
							
							if(stackOut[0]==10||stackOut[0]==13)
								return;
						}
						else
						{
							printf("RE\n");
							return ;
						}
					}
					 
				}
			}
	}

	
    return 0;
}
