/*****************************
 * Trabalho Pratico da disciplina de Compiladores
 * Professor: Alexei Machado
 * 
 * @author Darlan Francisco
 * @author Larissa Leite
 * @author Ygor Melo
 * @version 2/2021 
 * **************************/

import java.util.*;
import java.io.*;

// ANALISADOR SINTATICO E SEMANTICO
public class Compiladores extends AnalisadorLexico{

   // VARIAVEIS UTILIZADAS NO SINTATICO E NO SEMANTICO
   public static int tokenLido;
   public static String tipoEsperado;
   public static String tipoVar;
   public static String tipoEsperados[] = {"char","float","int","string"};
   public static Writer arquivo;

   // Metodo criado para identificar qual descrição informal da sintaxe das declarações e comandos da linguagem será chamado:
   // PROG -> {DEC | CMD} EOF 
   // S -> (1) {DEC | CMD} EOF (2)
   public static void PROG() throws Exception{

      // ZERA O ARQUIVO ASSEMBLY A CADA COMPILAÇÃO
      String nome = "tp4.asm";
      File f = new File(nome);
      f.delete();
      
      // CRIA O ARQUIVO ASSEMBLY
      arquivo = new BufferedWriter(new FileWriter("tp4.asm", true));
      
      // REGRA 1 DA GERAÇÃO DE CODIGO - PARTE 4
      arquivo.append("section .data\n");
      arquivo.append("M:\n");
      arquivo.append("   resb 10000h\n");
      arquivo.append("section .text\n");  
      arquivo.append("global _start\n");  
      arquivo.append("_start:\n"); 

      while(tokenLido == INT || tokenLido == CHAR  || tokenLido == STRING || tokenLido == FLOAT|| tokenLido == CONST || tokenLido == PONTO_VIRGULA | tokenLido == IF || tokenLido == WHILE || 
         tokenLido == WRITE  || tokenLido == WRITELN || tokenLido == READLN || tokenLido == ID){
         if(tokenLido == INT || tokenLido == CHAR || tokenLido == STRING || tokenLido == FLOAT || tokenLido == CONST){
            DEC();
         }
         else{
            CMD();
         }
      }
      CASATOKEN(Alfabeto.EOF);

      // REGRA 2 DA GERAÇÃO DE CODIGO PARTE 4
      arquivo.append("   mov rax, 60\n");
      arquivo.append("   mov rdi, 0\n");
      arquivo.append("   syscall\n");

      // FINALIZA O ARQUIVO
      arquivo.close();
   }

   // Metodo criado para dividir entre declaracao de variavel e declaracao de constante
   // DEC -> DEC_C | DEC_V
   public static void DEC() throws Exception{   
      if(tokenLido == CONST){
         DEC_C();
      } else if(tokenLido == INT || tokenLido == CHAR || tokenLido == STRING || tokenLido == FLOAT){
         DEC_V();
      }
   }

   // Metodo criado para declaracao de constante
   // DEC_C -> const id = valor;
   // valor -> CONS
   // Decl -> const Id(5) = [-]Constante; 
   // DEC_C -> const id = [-] valor (3);
   public static void DEC_C() throws Exception{
      CASATOKEN(CONST);

      // REGRA 5 DO SEMANTICO - PARTE 3
      for(int i = 34; i < alf.tabela.size(); i++){
         if(alf.tabela.get(i).lexema.equals(lexema.trim()) == true){
            if(alf.tabela.get(i).declaracao == 1){
               error.err_decID(lexema);
            } 
            // DEFINIÇÃO PARA A DECLARAÇÃO DO ID
            alf.tabela.get(i).declaracao = 1; // DECLARADO COMO SIM 
            alf.tabela.get(i).tipo = "const";
         }
      }
      
      CASATOKEN(ID);
      CASATOKEN(IGUAL);
      
      String auxSIMB = "";

      if(tokenLido == MENOS){
         auxSIMB = lexema;
         CASATOKEN(MENOS); 
      }else if(tokenLido == MAIS){
         CASATOKEN(MAIS);
      }

      // CRIADO PARA PASSAR O VALOR CORRETO PARA O ASSEMBLY
      String auxCONS = lexema;
      CASATOKEN(CONS);

      // REGRA 3 DA GERAÇÃO DE CODIGO - PARTE 4
      arquivo.append("; Geracao de declaracao de constantes\n");
      arquivo.append("section .data\n");
     
      if(regLex.tipo == "int" || regLex.tipo == "float"){
         if(auxSIMB == ""){
            arquivo.append("   dd "); 
            arquivo.append(auxCONS);
            arquivo.append("\n");
         } else {
            arquivo.append("   dd -"); 
            arquivo.append(auxCONS);
            arquivo.append("\n");
         }
      }
      else if(regLex.tipo == "char"){
         arquivo.append("   db ");
         arquivo.append(auxCONS);
         arquivo.append("\n");
      } else {
         arquivo.append("   db ");
         arquivo.append(auxCONS);
         arquivo.append(", 0");
         arquivo.append("\n");
      }
      arquivo.append("section .text\n");

      CASATOKEN(PONTO_VIRGULA);
   }

   // Metodo criado para declaracao de variveis
   // DEC_V -> (float | int | string | char) id[<- valor] { , id [<- valor]};
   // valor -> CONS
   // Decl -> (int| float| string | char) Id(1)(2) [<- [-]Constante(3)(4)] {, Id(1)(2) [<- [-]Constante(3)]} ;
   public static void DEC_V() throws Exception{ 
      
      // REGRA 2 DO SEMANTICO - PARTE 3
      if(tokenLido == INT){
         tipoEsperado = "int";
         CASATOKEN(INT);
      }else if(tokenLido == FLOAT){
         tipoEsperado = "float";
         CASATOKEN(FLOAT);
      }else if(tokenLido == STRING){
         tipoEsperado = "string";
         CASATOKEN(STRING);
      }else{
         tipoEsperado = "char";
         CASATOKEN(CHAR);
      }

      // REGRA 1 DO SEMANTICO - PARTE 3
      int auxVerificarID = 0;
      for(int i = 34; i < alf.tabela.size(); i++){
         if(alf.tabela.get(i).lexema.equals(lexema.trim()) == true){
            tipoVar = alf.tabela.get(i).tipo;
            auxVerificarID = i;
            if(alf.tabela.get(i).declaracao == 1){
               error.err_decID(lexema);
            }             
            alf.tabela.get(i).declaracao = 1;
            tipoVar = alf.tabela.get(i).tipo;
         }
      }
      regLex.declaracao = 1;
      CASATOKEN(ID);
      if(tokenLido == ATRIBUICAO){
         CASATOKEN(ATRIBUICAO);  
         if(tokenLido == MENOS){
            CASATOKEN(MENOS);

            // REGRA 3 DO SEMANTICO - PARTE 3
            if(tipoEsperado.equals("string") || tipoEsperado.equals("char")){
               error.err_tip();
            }
         }
         tipoEsperado = regLex.tipo;

         // REGRA 4 DO SEMANTICO - PARTE 3
         if(!tipoEsperado.equals(tipoVar)){
            error.err_tip();
         }
         
         CASATOKEN(CONS);
      }
      while(tokenLido == VIRGULA){
         CASATOKEN(VIRGULA);
         if(tokenLido == ID){

            // REGRA 1 DO SEMANTICO - PARTE 3
            for(int i = 34; i < alf.tabela.size(); i++){
               if(alf.tabela.get(i).lexema.equals(lexema.trim()) == true){
                  auxVerificarID = i;
                  if(i != alf.tabela.size()-1 || regLex.posicao == -1){
                     error.err_decID(lexema);
                  }
                  alf.tabela.get(i).declaracao = 1;
                  tipoVar = alf.tabela.get(i).tipo;
               }
            }
            regLex.declaracao = 1; 
            CASATOKEN(ID);
         }  
         //CASATOKEN(ID);
         if(tokenLido == ATRIBUICAO){
            CASATOKEN(ATRIBUICAO);
            if(tokenLido == MENOS){
               CASATOKEN(MENOS);
            }
            alf.tabela.get(auxVerificarID).tamanho = Integer.parseInt(lexema);
            tipoEsperado = regLex.tipo;

            // REGRA 3 DO SEMANTICO - PARTE 3
            if(!tipoEsperado.equals("float")||tipoEsperado.equals("int")){
               error.err_tip();
            }

            CASATOKEN(CONS);
         }
      }
      CASATOKEN(PONTO_VIRGULA);
   }

   // Metodo criado para dividir entre comando de teste, comando de repetição, comando de atribuição, comando de escrita e comando de leitura
   // CMD -> CMD_T | CMD_L | CMD_A | CMD_E | CMD_LR | ;
   public static void CMD() throws Exception{
      if(tokenLido == IF){
         CMD_T();
      }else if(tokenLido == WHILE){
         CMD_L();
      }else if(tokenLido == ID){
         CMD_A();
      }else if(tokenLido == READLN){
         CMD_LR();
      }else if(tokenLido == WRITELN){
         CMD_E();
      }else if (tokenLido == WRITE ){
         CMD_E();
      } 
      else {
         CASATOKEN(PONTO_VIRGULA);
      }
   }

   // Metodo criado para comando de teste
   // CMD_T -> if EXP (CMD | '{' {CMD} '}') [else (CMD | '{' {CMD} '}')]
   // CMD_T -> if EXP (4) (CMD | '{' {CMD} '}') (5) [else (CMD | '{' {CMD} '}')] (6) 
   public static void CMD_T() throws Exception{
      Simbolo auxExp = new Simbolo();
      CASATOKEN(IF);
      auxExp = EXP();

      // REGRA 4 DA GERAÇÃO DE CODIGO - PARTE 4
      String rotoloFimIf = Rotulos.geraRotulo();
      arquivo.append(rotoloFimIf + ":\n");
      arquivo.append("   mov eax, [M + " + auxExp.endereco + "]\n");
      arquivo.append("   cmp eax, 0 \n");
      arquivo.append("   je " + rotoloFimIf + "\n");

      if(auxExp.tipo != "boolean"){
         error.err_tip();
      }
      if(tokenLido == ABRE_CHAVE){
         CASATOKEN(ABRE_CHAVE);
         while(tokenLido != FECHA_CHAVE) {
            CMD();
         }
         CASATOKEN(FECHA_CHAVE);
      } else {
         CMD();
      }

      // REGRA 5 DA GERAÇÃO DE CODIGO - PARTE 4
      String rotuloFimElse= Rotulos.geraRotulo();
      arquivo.append(rotuloFimElse + ":\n");
      arquivo.append(" jmp " +  rotuloFimElse + "\n");
      arquivo.append(rotuloFimElse + ":\n");

      if(tokenLido == ELSE){
         CASATOKEN(ELSE);
         if(tokenLido == ABRE_CHAVE){
            CASATOKEN(ABRE_CHAVE);
            while(tokenLido != FECHA_CHAVE) {
               CMD();
            }
            CASATOKEN(FECHA_CHAVE);
         }
         else {
            CMD();
         }
      }

      // REGRA 6 DA GERAÇÃO DE CODIGO - PARTE 4
      arquivo.append(rotuloFimElse + ":");
   }

   // Metodo criado para comando de repeticao
   // CMD_L -> while EXP (CMD | '{' {CMD} '}')
   // CMD_L -> while (7) EXP (8) (CMD | '{' {CMD} '}') (9)
   public static void CMD_L() throws Exception{
      Simbolo exp1 = new Simbolo();
      CASATOKEN(WHILE);
      
      // REGRA 7 DA GERAÇÃO DE CODIGO - PARTE 4
      String rotoloInicialWhile = Rotulos.geraRotulo();
      arquivo.append(rotoloInicialWhile + ":\n");

      exp1 = EXP();

      if(exp1.tipo != "boolean"){
         error.err_tip();
      }

      // REGRA 8 DA GERAÇÃO DE CODIGO - PARTE 4
      String rotuloFimWhile = Rotulos.geraRotulo();
      arquivo.append("   mov eax, [M + " + exp1.tamanho + "]\n");
      arquivo.append("   cmp eax, 0\n");
      arquivo.append("   je " + rotuloFimWhile + "\n");

      if(tokenLido == ABRE_CHAVE){
         CASATOKEN(ABRE_CHAVE);
         while(tokenLido != FECHA_CHAVE){
            CMD();
         }
         CASATOKEN(FECHA_CHAVE);
      }
      else{
         CMD();
      }

      // REGRA 9 DA GERAÇÃO DE CODIGO - PARTE 4
      arquivo.append(rotuloFimWhile + ":\n");
   }

   // Metodo criado para comando de atribuicao
   // CMD_A -> id ['[' EXP']'] <- EXP ; 
   // Atr -> Id(7)(8) ['[' Exp(9) ']'] <- Exp(10)(11) ;
   public static void CMD_A() throws Exception{
      Simbolo exp1 = new Simbolo();
      // REGRA 7 DO SEMANTICO - PARTE 3
      // REGRA 8 DO SEMANTICO - PARTE 3
      int auxVerificarID = 0;
      for(int i = 34; i < alf.tabela.size(); i++){
         if(alf.tabela.get(i).lexema.equals(lexema.trim()) == true){
            tipoVar = alf.tabela.get(i).tipo;
            auxVerificarID = i;
            if(alf.tabela.get(i).declaracao == 0){
               error.err_decNot(lexema);
            } else if(!(alf.tabela.get(i).tipo.equals("char") || alf.tabela.get(i).tipo.equals("float") ||
                         alf.tabela.get(i).tipo.equals("int") || alf.tabela.get(i).tipo.equals("string")  )){
               error.err_class(lexema);
            }
         }
      }
      System.out.println(lexema);
      CASATOKEN(ID);

      // REGRA 10 DO SEMANTICO - PARTE 3
      // REGRA 11 DO SEMANTICO - PARTE 3
      if(!tipoVar.equals("string") && !tipoVar.equals("char") && !tipoVar.equals("int") && !tipoVar.equals("float")
         && alf.tabela.get(auxVerificarID).tamanho > 0 
         && tokenLido != ABRE_COLCHETE){
            error.err_tip();
      }
      if(tokenLido == ABRE_COLCHETE){
         CASATOKEN(ABRE_COLCHETE);
         if(tipoVar.equals("float")){
            error.err_tip();
         } else  {
         exp1.tipo = EXP().tipo;

         // REGRA 9 DO SEMANTICO - PARTE 3
         if(!(regLex.tipo.equals("int"))){
            error.err_tip();
         }
      }
         CASATOKEN(FECHA_COLCHETE);
      }
      CASATOKEN(ATRIBUICAO);
      exp1.tipo = EXP().tipo;
      CASATOKEN(PONTO_VIRGULA);
   }

   // Metodo criado para comando de escrita
   // CMD_E -> (write | writeln) '(' EXP {, EXP } ')' ;
   public static void CMD_E() throws Exception{
      if(tokenLido == WRITE){
         CASATOKEN(WRITE);
      }
      else{
         CASATOKEN(WRITELN);
      }
      CASATOKEN(ABRE_PARENTESES);
      EXP();
      while(tokenLido == VIRGULA){
         CASATOKEN(VIRGULA);
         EXP();
      }
      CASATOKEN(FECHA_PARENTESES);
      CASATOKEN(PONTO_VIRGULA);
   }

   // Metodo criado para comando de leitura
   // CMD_LR -> readln '(' id ')' ;
   // R -> readln '(' Id (7)(8) ')' ;
   public static void CMD_LR() throws Exception{
      if(tokenLido == READLN){
         CASATOKEN(READLN);
         CASATOKEN(ABRE_PARENTESES);

         // REGRA 7 DO SEMANTICO - PARTE 3
         // REGRA 8 DO SEMANTICO - PARTE 3
         int auxVerificarID = 0;
         for(int i = 34; i < alf.tabela.size(); i++){
            if(alf.tabela.get(i).lexema.equals(lexema.trim()) == true){
               if(alf.tabela.get(i).declaracao == 0){
                  error.err_decNot(lexema);
               }
               tipoVar = alf.tabela.get(i).tipo;
               auxVerificarID = i;
            }
         }
         if(tipoVar.equals("const")){
            error.err_class(lexema);
         }
         CASATOKEN(ID);
         CASATOKEN(FECHA_PARENTESES);
         CASATOKEN(PONTO_VIRGULA);
      }
   }

   // Metodo inicial para definicao de expressões
   // EXP -> SS [(= | != | < | > | <=| >=) SS ]
   // Exp -> S(12) [(=|!=|<|>|<=|>=) S(13)]
   public static Simbolo EXP() throws Exception{
      Simbolo auxEXP1 = new Simbolo();
      Simbolo auxEXP2 = new Simbolo();
      int operador = 0;

      // REGRA 12 DO SEMANTICO - PARTE 3
      auxEXP1.tipo = SS().tipo;
      auxEXP2.tipo = auxEXP1.tipo;

      if(tokenLido == IGUAL || tokenLido == DIFERENTE || tokenLido == MENOR || tokenLido == MAIOR || tokenLido == MENOR_IGUAL || tokenLido == MAIOR_IGUAL){
         operador = 0;
         if(tokenLido == IGUAL){
            operador = 1;
            CASATOKEN(IGUAL);
         } else if(tokenLido == DIFERENTE){
            operador = 2;
            CASATOKEN(DIFERENTE);
         } else if(tokenLido == MENOR){
            operador = 3;
            CASATOKEN(MENOR);
         } else if(tokenLido == MAIOR){
            operador = 4;
            CASATOKEN(MAIOR);
         } else if(tokenLido == MENOR_IGUAL){
            operador = 5;
            CASATOKEN(MENOR_IGUAL);
         } else if(tokenLido == MAIOR_IGUAL){
            operador = 6;
            CASATOKEN(MAIOR_IGUAL);
         }
         Simbolo auxEXP3 = new Simbolo();
         auxEXP3.tipo = SS().tipo;

         // REGRA 13 DO SEMANTICO - PARTE 3
         if (operador == 1) {
            if (auxEXP1.tipo.equals("string") && auxEXP3.tipo.equals("string")
                    || auxEXP1.tipo.equals("int") && auxEXP3.tipo.equals("int")
                    || auxEXP1.tipo.equals("float") && auxEXP3.tipo.equals("float")
                    || auxEXP1.tipo.equals("char") && auxEXP3.tipo.equals("char")
                    || auxEXP1.tipo.equals("const") && auxEXP3.tipo.equals("const")){
                     auxEXP2.tipo = "boolean";
            } else {
               error.err_tip();
            }
         } 
         else{
            if(auxEXP1.tipo.equals("int") && auxEXP3.tipo.equals("int")
            || auxEXP1.tipo.equals("int") && auxEXP3.tipo.equals("const")
            || auxEXP1.tipo.equals("const") && auxEXP3.tipo.equals("int")
            || auxEXP1.tipo.equals("float") && auxEXP3.tipo.equals("float")
            || auxEXP1.tipo.equals("const") && auxEXP3.tipo.equals("const")){
               auxEXP2.tipo = "boolean";
            } else{
               error.err_tip();
            }
         } 
      }
      return auxEXP2; 
   }

   // Metodo criado para tokens de operações bases e conjuncao
   // SS -> [-] TR {(+ | - | || ) TR }
   // S -> [-] T(14) {(+|-|'||') T(15)}
   public static Simbolo SS() throws Exception{
      
      boolean sinalMenos = false;
      boolean operadorMenos = false;
      Simbolo expS = new Simbolo();
      Simbolo auxT = new Simbolo();
      Simbolo auxT1 = new Simbolo();
      int operador = 0;
      
      if(tokenLido == MENOS){
         sinalMenos = true;
         CASATOKEN(MENOS);
      }

      // REGRA 14 DO SEMANTICO - PARTE 3
      auxT.tipo = TR().tipo;
      expS.tipo = auxT.tipo;

      while(tokenLido == MAIS || tokenLido == MENOS || tokenLido == OU){
         if(tokenLido == OU){
            operador = 0;
            CASATOKEN(OU);
         } else if(tokenLido == MAIS){
            operador = 1;
            CASATOKEN(MAIS);
         } else{
            operador = 2;
            CASATOKEN(MENOS);
            operadorMenos = true;
         }
         auxT1.tipo = TR().tipo;

         // REGRA 15 DO SEMANTICO - PARTE 3
         if((regLex.tipo == "int")){
            if(operador == 0){
               if(auxT.tipo != "boolean" && auxT1.tipo != "boolean"){
                  error.err_tip();
               } else if (auxT.tipo == auxT1.tipo){
                  error.err_tip();
               } else{
                  expS.tipo = "boolean";
               }
            } else if(operador == 1){
               if(auxT.tipo.equals("string") && auxT1.tipo.equals("string")){
                  expS.tipo = "string";
               } else if(auxT.tipo.equals("int") && auxT1.tipo.equals("int")){
                  expS.tipo = "int";
               } else if(auxT.tipo.equals("float") && auxT1.tipo.equals("float")){
                  expS.tipo = "float";
               } else if(auxT.tipo.equals("char") && auxT1.tipo.equals("char")){
                  expS.tipo = "char";
               } else{
                  error.err_tip();
               }
            } else if(operador == 2){
               if(auxT.tipo.equals("int") && auxT1.tipo.equals("int")){
                  expS.tipo = "int";
               } else if (auxT.tipo.equals("float") && auxT1.tipo.equals("float")){
                  expS.tipo = "float";                           
               } else{
                  error.err_tip();
               }
            }
         } else {
            if(operadorMenos && auxT1.tipo == "int"){
               auxT1.tipo = "int";
            }else if(operadorMenos && auxT1.tipo == "float"){
               auxT1.tipo = "float";
            }else{
               error.err_tip();
            }
            if(operador == 0){
               if(auxT.tipo != "boolean" && auxT1.tipo != "boolean"){
                  error.err_tip();
               } else if (auxT.tipo == auxT1.tipo){
                  error.err_tip();
               } else{
                  expS.tipo = "boolean";
               }
            } else if(operador == 1){
               if(auxT.tipo.equals("string") && auxT1.tipo.equals("string")){
                  expS.tipo = "string";
               } else if(auxT.tipo.equals("int") && auxT1.tipo.equals("int")){
                  expS.tipo = "int";
               } else if(auxT.tipo.equals("float") && auxT1.tipo.equals("float")){
                  expS.tipo = "float";
               } else if(auxT.tipo.equals("char") && auxT1.tipo.equals("char")){
                  expS.tipo = "char";
               } else{
                  error.err_tip();
               }
            } else if(operador == 2){
               if(auxT.tipo.equals("int") && auxT1.tipo.equals("int")){
                  expS.tipo = "int";
               } else if (auxT.tipo.equals("float") && auxT1.tipo.equals("float")){
                  expS.tipo = "float";                           
               } else{
                  error.err_tip();
               }
            }
         }
      }
      return expS;
   }  

   // Metodo criado para tokens de operações e junção
   // TR -> FAT {( * | / | && | div | mod ) FAT}
   // T -> F(16) {(*|/|&&|div|mod) F(17)}
   public static Simbolo TR() throws Exception{
      Simbolo auxF = new Simbolo();
      Simbolo auxF1 = new Simbolo();
      Simbolo auxT = new Simbolo();
      
      // REGRA 16 DO SEMANTICO - PARTE 3
      auxF.tipo = FAT().tipo;
      auxT.tipo = auxF.tipo;

      int operador = 0;
      while(tokenLido == MULTIPLICACAO || tokenLido == DIVISAO || tokenLido == AND || tokenLido == DIV || tokenLido == MOD){
         if(tokenLido == MULTIPLICACAO){
            operador = 0;
            CASATOKEN(MULTIPLICACAO);
         }
         else if(tokenLido == DIVISAO){
            operador = 1;
            CASATOKEN(DIVISAO);
         }
         else if(tokenLido == AND){
            operador = 2;
            CASATOKEN(AND);
         }
         else if(tokenLido == DIV){
            operador = 3;
            CASATOKEN(DIV);
         }
         else if(tokenLido == MOD){
            operador = 4;
            CASATOKEN(MOD);
         }

         auxF1.tipo = FAT().tipo;

         // REGRA 17 DO SEMANTICO - PARTE 3
         if(operador == 0){
            if(auxF.tipo.equals("int")&& auxF1.tipo.equals("int")
              || auxF.tipo.equals("float")&& auxF1.tipo.equals("float")
               ||auxF.tipo.equals("int")&& auxF1.tipo.equals("float")
               ||auxF.tipo.equals("float")&& auxF1.tipo.equals("float")

               ){
               auxT.tipo = "int";
            }
            else if(auxF.tipo.equals("float")&& auxF1.tipo.equals("float")){
               auxT.tipo = "float";
            }
            else{
               error.err_tip();
            }
         } else if (operador == 1){
            //System.out.println("TIPO: "+ auxF.tipo);
            //System.out.println("TIPO: "+ auxF1.tipo);
            if(auxF.tipo.equals("int")&& auxF1.tipo.equals("int")
               || auxF.tipo.equals("int")&& auxF1.tipo.equals("const")
               || auxF.tipo.equals("const")&& auxF1.tipo.equals("int")
               || auxF.tipo.equals("float")&& auxF1.tipo.equals("float")
               || auxF.tipo.equals("float")&& auxF1.tipo.equals("const")
               || auxF.tipo.equals("float")&& auxF1.tipo.equals("int")
               ){
               auxT.tipo = "boolean";
            }
            if(auxF.tipo.equals("int") && auxF1.tipo.equals("const")){
               auxT.tipo = "int";
            }
            else{
               error.err_tip();
            }
         } else if (operador == 2){
            if(auxF.tipo.equals("int")&& auxF1.tipo.equals("int")){
               auxT.tipo = "int";
            }
            else if(auxF.tipo.equals("float") && auxF1.tipo.equals("float")){
               auxT.tipo = "float";
            }
            else{
               error.err_tip();
            }
         } else if (operador == 3){
            if(auxF.tipo.equals("int")&& auxF1.tipo.equals("int")){
               auxT.tipo = "int";
            }
            else if(auxF.tipo.equals("float")&& auxF1.tipo.equals("float")){
               auxT.tipo = "float";
            }
            else{
               error.err_tip();
            }
         } else if (operador == 4){
            if(auxF.tipo.equals("int")&& auxF1.tipo.equals("int")){
               auxT.tipo = "int";
            }
            else if(auxF.tipo.equals("float")&& auxF1.tipo.equals("float")){
               auxT.tipo = "float";
            }
            else{
               error.err_tip();
            }
         } 
      }
      return auxT;
   }

   // Metodo criado para fator da expressão
   // FAT -> ! FAT | '(' EXP ')' | ID ['[' EXP ']'] | CONS | (INT | FLOAT) '(' EXP ')'
   // F -> ! F(18) | '(' Exp ')'(19) | Id(7)['[' Exp(9) ']'] | Constante(6) | (int| float) '('Exp')'
   // FAT -> ! FAT | '(' EXP ')' | ID ['[' EXP ']'] | CONS (10) | (INT | FLOAT) '(' EXP ')'
   public static Simbolo FAT() throws Exception{  
      
      Simbolo auxF = new Simbolo();
      Simbolo auxF1 = new Simbolo();

      if(tokenLido == NOT){
         CASATOKEN(NOT);
         auxF1.tipo = FAT().tipo;

         // REGRA 18 DO SEMANTICO - PARTE 3
         if(auxF1.tipo != "boolean"){
            error.err_tip();
         }
      } 
      else if(tokenLido == ABRE_PARENTESES){
         CASATOKEN(ABRE_PARENTESES);

         // REGRA 19 DO SEMANTICO - PARTE 3
         auxF.tipo = EXP().tipo;

         CASATOKEN(FECHA_PARENTESES);
      } 
      else if(tokenLido == ID){
         
         // REGRA 7 DO SEMANTICO - PARTE 3
         int auxVerificarID = 0;
         for(int i = 34; i < alf.tabela.size(); i++){
            if(alf.tabela.get(i).lexema.equals(lexema.trim()) == true){
               auxVerificarID = i;
               if(alf.tabela.get(i).declaracao == 0){
                  error.err_decNot(lexema);
               } 
               tipoVar = alf.tabela.get(i).tipo;
            } else{
               auxF.tipo = tipoVar;
            }
         }
         CASATOKEN(ID);
         if(!tipoVar.equals("string") && !tipoVar.equals("char") &&
            alf.tabela.get(auxVerificarID).tamanho > 0 && tokenLido != ABRE_COLCHETE){
            error.err_tip();
         }
         if(tokenLido == ABRE_COLCHETE){
            if(alf.tabela.get(auxVerificarID).tamanho == 0){
               error.err_tip();
            }
            CASATOKEN(ABRE_COLCHETE);
            auxF.tipo = EXP().tipo;
            CASATOKEN(FECHA_COLCHETE);
         }
      } 
      else if(tokenLido == INT || tokenLido == FLOAT){
         if(tokenLido == INT){
            CASATOKEN(INT);
         } else {
            CASATOKEN(FLOAT);
         }
         CASATOKEN(ABRE_PARENTESES);
         auxF.tipo = EXP().tipo;
         CASATOKEN(FECHA_PARENTESES);
      } 
      else {
         auxF.tipo = regLex.tipo;
         CASATOKEN(CONS);

         // REGRA 10 DA GERAÇÃO DE CODIGO - PARTE 4
         if(tipoVar == "int"){
            // FAT1.endereco = novoTemporario(const.tamanhoConstante);
            arquivo.append("   mov eax, " + lexema + "\n");
            arquivo.append("   mov [M + " + auxF.tamanho + "], eax" + "\n");
         } else if(tipoVar == "string"){
            // FAT1.endereco = alocaMemoria(const.tamanhoConstante);
            arquivo.append("section .data" + "\n");
            arquivo.append("   db  " + lexema + " ,0    " + "\n");
            arquivo.append("section .text" + "\n");
         }
      }
      return auxF;
   }

   // UTILIZADADO PARA LER O TOKEN
   public static void CASATOKEN(int tokenEsperado)throws Exception{
      tokenLido = regLex.token;
      // Utilizado para testes e comparações
      // System.out.println("TOKEN LER: " + tokenLido);
      // System.out.println("TOKEN ESP: " + tokenEsperado);
      // Comparação para tomada de decisão do programa
      if(tokenLido == tokenEsperado && tokenLido != Alfabeto.EOF){ 
         lexema = "";
         Lexico();
         // Utilizado para testes e comparações
         // System.out.println("TOKEN: " + tokenLido);
         // System.out.println("TOKEN:");
         // Retorno do token
         tokenLido = regLex.token;
      }
      // Utilizado para definição ou saída de erros
      else if(tokenLido == Alfabeto.EOF){
         if(tokenLido != tokenEsperado){
            // Erro para o fim de arquivo
            error.err_arq();
         }
         else{
            System.out.println(linhasCompiladas + " linhas compiladas.");
         }
      }
      else{
         // Erro de token
         error.err_tok(lexema);
      }
   }
   
   public static void main(String[]args){
      // Definir a chamada do analisador sintatico
      try {
         Lexico();
         tokenLido = regLex.token;
         PROG();
      } catch (Exception e) {
         System.out.println(linhasCompiladas);
         System.out.println(e.getMessage());
      }
   }
}

// CLASSE GERAÇÃO DE ROTULOS
class Rotulos {

   static int contador = 0;

   // PEGA O PROXIMO ROTULO LIVRE
   static String geraRotulo() {
      String retorno = "Rot" + contador;
      contador++;
      return retorno;
   }
}

// CLASSE DE ERRO
class Error_lexico{
   
   // ERRO DE CHAR
   public void err_char() throws Exception{
      throw new Exception("caractere invalido.");
   }

   // ERRO DE FIM DE ARQUIVO
   public void err_arq() throws Exception{
      throw new Exception("fim de arquivo nao esperado.");
   }

   // ERRO DE LEXEMA
   public void err_lex(String lexema) throws Exception{
      throw new Exception("lexema nao identificado [" + lexema + "].");
   }

   // ERRO DE TOKEN
   public void err_tok(String lexema) throws Exception{
      throw new Exception("token nao esperado [" + lexema + "].");
   }

   // ERRO DE IDENTIFICADOR DECLARADO
   public void err_decID(String lexema) throws Exception{
      throw new Exception("identificador ja declarado [" + lexema + "].");
   }

   // ERRO DE IDENTIFICADOR NÃO DECLARADO
   public void err_decNot(String lexema) throws Exception{
      throw new Exception("identificador nao declarado ["+ lexema + "].");
   }

   // ERRO TIPOS INCOMPATIVEIS
   public void err_tip() throws Exception{
      System.out.println("lexema");
      throw new Exception("tipos incompativeis.");
   }

   // ERRO DE CLASSE NÃO COMPATIVEL
   public void err_class(String lexema) throws Exception{
      throw new Exception("classe de identificador incompativel [" + lexema + "].");
   }
}

// CLASSE DEFINIÇÃO DO ALFABETO
class Alfabeto {
   
   // ARRAYLIST QUE CONTEM A TABELA COM OS SIMBOLOS E O TOKEN
   List<Simbolo> tabela = new ArrayList<>();
   public static int   CONST = 0,
                  INT = 1,
                  CHAR = 2,
                  WHILE = 3,
                  IF = 4,
                  FLOAT = 5,
                  ELSE = 6,
                  AND = 7,
                  OU = 8,
                  NOT = 9,
                  ATRIBUICAO = 10,
                  IGUAL = 11,
                  ABRE_PARENTESES = 12,
                  FECHA_PARENTESES = 13,
                  MENOR = 14,
                  MAIOR = 15,
                  DIFERENTE = 16,
                  MENOR_IGUAL = 17,
                  MAIOR_IGUAL = 18,
                  VIRGULA = 19,
                  MAIS = 20,
                  MENOS = 21,
                  MULTIPLICACAO = 22,
                  DIVISAO = 23,
                  PONTO_VIRGULA = 24,
                  ABRE_CHAVE = 25,
                  FECHA_CHAVE = 26,
                  READLN = 27,
                  DIV = 28,
                  WRITE = 29,
                  WRITELN = 30,
                  MOD = 31,
                  ABRE_COLCHETE = 32,
                  FECHA_COLCHETE = 33,
                  ID = 34,
                  EOF = 35,
                  STRING = 36,
                  CONS = 37;
   
   // ALFABETO PARA DEFINIÇÃO DA TABELA COM OS SIMBOLOS E O TOKEN
   public Alfabeto(){
      tabela.add(new Simbolo(CONS, "cons"));
      tabela.add(new Simbolo(CONST,"const")); 
      tabela.add(new Simbolo(INT,   "int"));
      tabela.add(new Simbolo(CHAR, "char"));
      tabela.add(new Simbolo(WHILE, "while"));
      tabela.add(new Simbolo(IF, "if"));
      tabela.add(new Simbolo(FLOAT, "float"));
      tabela.add(new Simbolo(ELSE, "else"));
      tabela.add(new Simbolo(AND, "&&"));
      tabela.add(new Simbolo(OU, "||"));
      tabela.add(new Simbolo(NOT, "!"));
      tabela.add(new Simbolo(ATRIBUICAO, "<-"));
      tabela.add(new Simbolo(IGUAL, "="));
      tabela.add(new Simbolo(ABRE_PARENTESES, "("));
      tabela.add(new Simbolo(FECHA_PARENTESES, ")"));
      tabela.add(new Simbolo(MENOR, "<"));
      tabela.add(new Simbolo(MAIOR, ">"));
      tabela.add(new Simbolo(DIFERENTE, "!="));
      tabela.add(new Simbolo(MENOR_IGUAL, "<="));
      tabela.add(new Simbolo(MAIOR_IGUAL, ">="));
      tabela.add(new Simbolo(VIRGULA, ","));
      tabela.add(new Simbolo(MAIS, "+"));
      tabela.add(new Simbolo(MENOS, "-"));
      tabela.add(new Simbolo(MULTIPLICACAO, "*"));
      tabela.add(new Simbolo(DIVISAO, "/"));
      tabela.add(new Simbolo(PONTO_VIRGULA, ";"));
      tabela.add(new Simbolo(ABRE_CHAVE, "{"));
      tabela.add(new Simbolo(FECHA_CHAVE, "}"));
      tabela.add(new Simbolo(READLN, "readln"));
      tabela.add(new Simbolo(DIV, "div"));
      tabela.add(new Simbolo(WRITE, "write"));
      tabela.add(new Simbolo(WRITELN, "writeln"));
      tabela.add(new Simbolo(MOD, "mod"));
      tabela.add(new Simbolo(ABRE_COLCHETE, "["));
      tabela.add(new Simbolo(FECHA_COLCHETE, "]"));
      tabela.add(new Simbolo(STRING, "string"));
   }
}

// CLASSE ONDE SÂO ARMAZENADOS OS SIMBOLOS
class Simbolo{

   int token;
   int posicao;
   int tamanho;
   int declaracao;  // DECLARACAO CRIADO PARA O SEMANTICO: 0 -> NÃO, 1 -> SIM (Verifica se o identificador já foi criado)
   String lexema;
   String tipo;
   int endereco;

   public Simbolo(){
      token = 0;
      posicao = 0;
      tamanho = 0;
      declaracao = 0;
      lexema = "";
      tipo = "";
      endereco = 0;
   }

   public Simbolo(int auxToken, String auxLex){
      token = auxToken;
      lexema = auxLex;
   }

   public Simbolo(int auxToken, String auxLex, int auxPosicao, String auxTipo){
      token = auxToken;
      lexema = auxLex;
      posicao = auxPosicao;
      tipo = auxTipo;
      tamanho = 0;
   }

   public Simbolo(int auxToken, String auxLex, String auxTipo, int auxTamanho, int auxEndereco){
      token = 0;
      lexema = "";
      posicao = 0;
      tipo = auxTipo;
      tamanho = auxTamanho;
      declaracao = 0;
      endereco = auxEndereco;
   }
}

// INICIO DO ANALISADOR LEXICO
class AnalisadorLexico extends Alfabeto{

   // LEITURA
   static BufferedReader ler = new BufferedReader(new InputStreamReader(System.in));

   // DECLARAÇÃO DOS OBJETOS
   public static Simbolo regLex = new Simbolo();                     // -> TABELA   
   public static Alfabeto alf = new Alfabeto();                      // -> ALFABETO
   public static Error_lexico error = new Error_lexico();            // -> ERRO
   
   // VARIAVEIS USADAS
   public static int linhasCompiladas = 1;
   public static char aux = '\0';
   public static String lexema = "";
   public static boolean flag = false;
   static boolean EOF = false;
   static int estadoI = 0;
   static String salvarTipo;

   // REALIZA LEITURA DO ARQUIVO
   public static char Leitura() throws Exception{
      int b = 0;
      char a ='\0';
      try {
         b = ler.read();
      } catch (IOException e) {
         EOF = true;
         regLex.token = Alfabeto.EOF;
         return a;
      }
      a = (char)b;
      if (b == -1) {
         if(estadoI > 0){
            error.err_arq();
         }
         else{
            regLex.token = Alfabeto.EOF;
            EOF = true;
         }
      }
      return a;
   }

   // ANALISA SE O CHAR E VALIDO , PARA DEFINIÇAO UTILIZAMOS A TABELA ASCII
   public static boolean isValido( char analisado ){
      boolean valido = true; 
      if(!((analisado == '\0')||(analisado == '_')||
          // 0x9 -> TAB , 0xA -> nova linha, 0xD -> enter
         (analisado == 0x9  || analisado == 0xA || analisado == 0xD) ||
         (analisado >= ' ' && analisado <= '"')|| 
         (analisado >= '&' && analisado <= '?')||
         (analisado >= 'A' && analisado <= ']')||
         (analisado >= 'a' && analisado <= '}')|| 
         (analisado == '$' || analisado == '%')||
         (analisado == '\'')
         )){
         valido = false;  
      }
      return valido;
   }

   // ANALISADOR LEXICO 
   public static void Lexico() throws Exception {
   
      // DEFINICAO DOS ESTADOS
      estadoI = 0;
      int estadoF = 18;
      char t;

      while(estadoI != estadoF){
         if(aux == '\0'){
            t = Leitura();
         } else {
            t = aux;
            aux = '\0';
         }
         // VERIFICA SE O É O FIM DO ARQUIVO CASO RETORNE VERDADEIRO FINALIZA O PROGRAMA
         if(EOF){
            if(estadoI != 0){
               error.err_arq();
            }
            regLex.token = Alfabeto.EOF;
            break;
         }
      
         // SE O CHAR FOR INVALIDO APRESENTARA O ERRO
         if((int) t >= 126){
            error.err_char();
         }
      
         // AUTOMATO IMPLEMENTADO NA FORMA DO SWITCH
         switch(estadoI){
            case 0:
               // valida inicio com 0, irá camar o case 1
               if(t == '0'){
                  estadoI = 1;
               // valida inicio com digito, limitado de 1 a 9, irá camar o case 2
               } else if(t >= '1' && t <= '9'){
                  estadoI = 2;
               // valida inicio com letra, maiusculas e minusculas são diferenciadas, irá camar o case 3
               } else if(t >= 'a' && t <= 'z' || t >= 'A' && t <= 'Z'){
                  estadoI = 3;
               // valida inicio com underline, irá camar o case 4
               } else if(t == '_'){
                  estadoI = 4;
               // valida inicio com aspas, irá camar o case 5
               } else if (t == '\"') {
                  estadoI = 5;
               // valida inicio com virgula, irá camar o case 18(fim do automato)
               } else if(t == ','){
                  regLex.token = Alfabeto.VIRGULA;
                  estadoI = 18;
               // valida inicio com ponto-virgula, irá camar o case 18(fim do automato)
               } else if(t == ';'){
                  regLex.token = Alfabeto.PONTO_VIRGULA;
                  estadoI = 18;
               // valida inicio com +, irá camar o case 18(fim do automato)
               } else if(t == '+'){
                  regLex.token = Alfabeto.MAIS;
                  estadoI = 18;
               // valida inicio com menos, irá camar o case 18(fim do automato)
               } else if(t == '-'){
                  regLex.token = Alfabeto.MENOS;
                  estadoI = 18;
               // valida inicio com igual, irá camar o case 18(fim do automato)
               } else if(t == '='){
                  regLex.token = Alfabeto.IGUAL;
                  estadoI = 18;
               // valida inicio com abre parenteses, irá camar o case 18(fim do automato)
               } else if(t == '('){
                  regLex.token = Alfabeto.ABRE_PARENTESES;
                  estadoI = 18;
               // valida inicio com fecha parenteses, irá camar o case 18(fim do automato)
               } else if(t == ')'){
                  regLex.token = Alfabeto.FECHA_PARENTESES;
                  estadoI = 18;
               // valida inicio com abre chaves, irá camar o case 18(fim do automato)
               } else if(t == '{'){
                  regLex.token = Alfabeto.ABRE_CHAVE;
                  estadoI = 18;
               // valida inicio com fecha chaves, irá camar o case 18(fim do automato)
               } else if(t == '}'){
                  regLex.token = Alfabeto.FECHA_CHAVE;
                  estadoI = 18;
               // valida inicio com abre colchete, irá camar o case 18(fim do automato)
               } else if(t == '['){
                  regLex.token = Alfabeto.ABRE_COLCHETE;
                  estadoI = 18;
               // valida inicio com fecha colchete, irá camar o case 18(fim do automato)
               } else if(t == ']'){
                  regLex.token = Alfabeto.FECHA_COLCHETE;
                  estadoI = 18;
               // valida inicio com maior, irá camar o case 6
               } else if(t == '>'){
                  estadoI = 6;
               // valida inicio com menor, irá camar o case 7
               } else if(t == '<'){
                  estadoI = 7;
               // valida inicio com asterisco, irá camar o case 8
               } else if(t == '*'){
                  estadoI = 8;
               // valida inicio com barra, irá camar o case 9
               } else if(t == '/'){
                  estadoI = 9;
               // valida inicio com aspas simples, irá camar o case 10
               } else if(t == '\''){
                  estadoI = 10;
               // valida inicio com & comercial, irá camar o case 11
               } else if(t == '&'){
                  estadoI = 11;
               // valida inicio com exclamação, irá camar o case 17
               } else if(t == '!'){
                  estadoI = 17;
               // valida inicio com ponto, irá camar o case 19
               } else if(t == '.'){
                  estadoI = 19;
               // valida inicio com pipe, irá camar o case 20
               } else if(t == '|'){
                  estadoI = 20;
               // valida inicio com espaço, quebra de linha, final de linha, irá camar o case 20
               } else if (t == ' ' || t == '\n' || t == '\r' || t == '\t' ) {
                  estadoI = 0;
                  break;
               //valida se é um inicio valido
               } else if(isValido(t)){
                  lexema += t;
                  //erro lexema
                  error.err_lex(lexema);
               } else {
                  //erro de caractere
                  error.err_char();  
               }
               lexema += t;
               break;
         
            // HEXADECIMAL, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 1:
               if (t == 'x') {
                  estadoI = 12;
               } else if (t >= '0' && t <= '9') {
                  estadoI = 2;
               }
               else if(t == '.'){
                  estadoI = 19;
               } else if (!(t >= '0' && t <= '9') && !(t == 'x')) {
                  regLex.token = Alfabeto.CONS;
                  regLex.tipo = "int";
                  estadoI = 18;
                  aux = t;
                  break;
               }
               else{
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
         
            // INTEIRO, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 2:
               if (t >= '0' && t <= '9') {
                  estadoI = 2;
               } 
               else if(t == '.'){
                  estadoI = 19;
               } 
               else if(!(t >= '0' && t <= '9') && !(t == '.')){
                  regLex.token = Alfabeto.CONS;
                  regLex.tipo = "int";
                  estadoI = 18;
                  aux = t;
                  break;
               } else {
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
   
            // IDENTIFICADOR, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 3:
               if ((t >= 'a' && t <= 'z')|| (t>='A' && t<='Z')
                     || (t == '_')
                        || (t >= '0' && t <= '9')|| t == '.'){
                        estadoI = 3;
               } else {
                  estadoI = 18;
                  aux = t;
                  for (int i = 0; i < alf.tabela.size(); i++) {
                     if(alf.tabela.get(i).lexema.equals(lexema.trim())==true){
                        regLex.token = alf.tabela.get(i).token;
                        flag = true;
                        // Verificar os tipos e salvar na variavel -> salvarTipo
                        // TIPOS: Caracter - real - inteiro - String
                        if(lexema.trim().equals("char") || lexema.trim().equals("float") || 
                           lexema.trim().equals("int") || lexema.trim().equals("string")){
                           salvarTipo = lexema;
                        }
                        else if(alf.tabela.get(i).token == Alfabeto.ID){
                           regLex.posicao = -1;
                        }
                        i = alf.tabela.size();
                     }
                  }
                  if(flag == false){
                     // ----- Teste ---- \\
                     // System.out.println("Colocou na tabela: " + lexema);
                     alf.tabela.add(new Simbolo(Alfabeto.ID, lexema, alf.tabela.size(), salvarTipo));
                     regLex.token = Alfabeto.ID;
                  }
                  // System.out.println(lexema);
                  flag = false; 
                  break;
               }
               lexema += t;
               break;
            
            // IDENTIFICADOR, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 4:
               if((t >= 'a' && t <= 'z') || (t> 'A' && t <= 'Z') || (t >= '0' && t <= '9') || t == '.'){ 
                  estadoI = 3;
               } else if (t == '_'){   
                  estadoI = 4;
               }
               else{
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
            
            // STRING, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 5:
               if ((!(t == '\"') && !(t == '$') && !(t == ('\n')) && !(t == ('\r')) && isValido(t))) {
                  estadoI = 5;
               } else if (t == '"') {
                  regLex.token = Alfabeto.CONS;
                  regLex.tipo = "string";
                  regLex.tamanho = lexema.length()-1;
                  estadoI = 18;
               } else if(t == '\0'){
                  error.err_arq();
               } else{
                  if((t == ('\n')) || (t == ('\r')) || (t == '"') || (t == '$')){
                     error.err_lex(lexema);
                  }
                  else{
                     error.err_char();
                  }
               }
               lexema += t;
               break;
         
            // MAIOR E MAIOR_IGUAL, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 6:
               if (t == '=') {
                  regLex.token=Alfabeto.MAIOR_IGUAL;
                  estadoI = 18;
               } else {
                  regLex.token=Alfabeto.MAIOR;
                  estadoI = 18;
                  aux = t;
                  break;
               }
               lexema += t;
               break;
         
            // MENOR E MENOR_IGUAL E ATRIBUICAO, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 7:
               if(t == '-'){
                  regLex.token = Alfabeto.ATRIBUICAO;
                  estadoI = 18;
               } else if(t == '='){
                  regLex.token = Alfabeto.MENOR_IGUAL;
                  estadoI = 18;
               } else {
                  regLex.token = Alfabeto.MENOR;
                  estadoI = 18;
                  aux = t;
                  break;
               }
               lexema += t;
               break;
         
            // MULTIPLICACAO, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 8:
               regLex.token = Alfabeto.MULTIPLICACAO;
               estadoI = 18;
               aux = t;
               lexema += 1;
               break;
         
            // DIVISAO, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 9:
               if (t == '*') {
                  estadoI = 14;
               } else {
                  regLex.token = Alfabeto.DIVISAO;
                  estadoI = 18;
                  aux = t;
                  break;
               }
               lexema += t;
               break;
         
            // POSSIVEL CHAR, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 10:
               if (isValido(t)){
                  if(t == '\''){
                     lexema += t;
                     error.err_char();
                  }
                  else{
                     estadoI = 16;
                  }
               } else{
                  error.err_char();
               }
               lexema += t;
               break;
         
            // AND, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 11:
               if(t == '&'){
                  regLex.token = Alfabeto.AND;
                  estadoI = 18;
               }
               else{
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
            
            // CHAR, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 12:
               if ((t >= 'A' && t <= 'F') || t >= '0' && t <= '9') {
                  estadoI = 13;
               } else{
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
         
            // CHAR, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 13:
               if ((t >= '0' && t <= '9') || (t >= 'A' && t <= 'F') ) {
                  regLex.token = Alfabeto.CONS;
                  regLex.tipo ="char";
                  estadoI = 18;
                  break;
               } else if (!(t >= '0' && t <= '9')|| !(t >= 'A' && t <= 'F')) {
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
            
            // COMENTARIO, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 14:
               if (t != '*') {
                  if(isValido(t)){
                    estadoI = 14;
                  }
                  else{
                    lexema += t;
                    error.err_lex(lexema);
                  }
               } else {
                  estadoI = 15;
               }
               lexema += t;
               break;

            // COMENTARIO, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 15:
               if (t == '/') {
                  estadoI = 0;
                  lexema = "";
                  break;
               } else if (t != '/') {
                  if(t == '*'){
                     estadoI = 15;
                  }
                  else if(isValido(t)){
                     estadoI = 14;
                  }
                  else{
                     lexema += t;
                     error.err_lex(lexema);
                  }
               }
               lexema += t;
               break;
            
            // CHAR, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 16:
               if (t == '\'') {
                  regLex.token = Alfabeto.CONS;
                  regLex.tamanho = 1;
                  regLex.tipo="char";
                  estadoI = 18;
               }
               else{
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
         
            // DIFERENTE E NOT, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 17:
               if(t == '='){
                  regLex.token = Alfabeto.DIFERENTE;
                  estadoI = 18;
               } else{
                  regLex.token = Alfabeto.NOT;
                  estadoI = 18;
               }
               lexema += t;
               break;
         
            // FIM AUTOMATO
            case 18:
               lexema="";
               break; 

            // FLOAT, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 19:
               if((t >= '0' && t <= '9') && (lexema.length() < 6)){
                  estadoI = 21;
               }
               else{
                  error.err_lex(lexema);
               }
               lexema += t;
               break;

            // OU, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 20:
               if(t == '|'){
                  regLex.token = Alfabeto.OU;
                  estadoI = 18;
               }else {
                  error.err_lex(lexema);
               }
               lexema += t;
               break;

            // FLOAT, a cada if ele valida o próximo caractere e define a próxima chamada do case ou o fim do analisador lexico
            case 21:
               if((t >= '0' && t <= '9') && (lexema.length() < 6)){
                  estadoI = 21;
               } else if(!(t >= '0' && t <= '9')){
                  regLex.token = Alfabeto.CONS; 
                  regLex.tipo = "float";
                  estadoI = 18;
                  aux = t;
                  break;
               } else{
                  error.err_lex(lexema);
               }
               lexema += t;
               break;
         }
      
         if(t == '\n' && aux == '\0') {
            linhasCompiladas++;
         }
      }
      regLex.lexema = lexema;   
   }     
}