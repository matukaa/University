#include <iostream>

using namespace std;

int main(){
char outmsg[4] = { '%', 'd', '\n', '\0' };
char inmsg[3] = { '%', 'd', '\0' };
int a;
int b;
int c;
int d;
__asm{
mov dword ptr [a], 5;
mov dword ptr [b], 5;
mov eax, [a];
sbb eax, [b];
mov [c], eax;
push eax;
push ebx;
push ecx;
push edx;
lea eax, [d];
push eax;
lea eax, [inmsg];
push eax;
call scanf;
add esp, 8;
pop edx;
pop ecx;
pop ebx;
pop eax;
mov eax, [c];
cmp eax, 0;
je else37;
push eax;
push ebx;
push ecx;
push edx;
mov eax, dword ptr [d];
push eax;
lea eax, [outmsg];
push eax;
call printf;
add esp, 8;
pop edx;
pop ecx;
pop ebx;
pop eax;
else37:

}
return 0;
}