package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     * 
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 添加一个符号
     * 
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        if (this.symbolTable.get(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.symbolTable.put(name, new SymbolEntry(isConstant, isInitialized, getNextVariableOffset()));
        }
    }

    /**
     * 设置符号为已赋值
     * 
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    /**
     * <程序> ::= 'begin'<主过程>'end'
     */
    private void analyseProgram() throws CompileError {
        // 示例函数，示例如何调用子程序
        // 'begin'
        expect(TokenType.Begin);

        analyseMain();

        // 'end'
        expect(TokenType.End);
        expect(TokenType.EOF);
    }

    private void analyseMain() throws CompileError {
        analyseConstantDeclaration();
        analyseVariableDeclaration();
        analyseStatementSequence();
    }

    private void analyseConstantDeclaration() throws CompileError {
        // 示例函数，示例如何解析常量声明
        // 如果下一个 token 是 const 就继续
        while (nextIf(TokenType.Const) != null) {
            // 变量名
            var nameToken = expect(TokenType.Ident);

            addSymbol(nameToken.getValueString(),false,true,nameToken.getStartPos());

            instructions.add(new Instruction(Operation.LIT, 0));
            // 等于号
            expect(TokenType.Equal);

            // 常表达式
            analyseConstantExpression();

            int bias=getOffset(nameToken.getValueString(),nameToken.getStartPos());
            instructions.add(new Instruction(Operation.STO,bias));

            declareSymbol(nameToken.getValueString(),nameToken.getStartPos());
            // 分号
            expect(TokenType.Semicolon);
        }
    }

    private void analyseVariableDeclaration() throws CompileError {
        while(nextIf(TokenType.Var)!=null)
        {
            var wor=expect(TokenType.Ident);

            instructions.add(new Instruction(Operation.LIT, 0));

            addSymbol(wor.getValueString(),false,false,wor.getStartPos());

            if(check(TokenType.Equal))
            {
                next();
                analyseExpression();
                int bias=getOffset(wor.getValueString(),wor.getStartPos());
                instructions.add(new Instruction(Operation.STO,bias));

                declareSymbol(wor.getValueString(),wor.getStartPos());
            }
            expect(TokenType.Semicolon);
        }
    }

    private void analyseStatementSequence() throws CompileError {
        analyseStatement();
    }

    private void analyseStatement() throws CompileError {
        while(true)
        {
            if(check(TokenType.Semicolon))
            {
                next();//空的
            }
            else if(check(TokenType.Print))
            {
                analyseOutputStatement();//输出语句
            }
            else if(check(TokenType.Ident)) {
                analyseAssignmentStatement();//赋值语句
            }
            else break;
        }
    }

    private void analyseConstantExpression() throws CompileError {
        int sig=0;
        if(check(TokenType.Plus))
        {
            next();
        }
        else if(check(TokenType.Minus))
        {
            next();
            sig=1;
        }

        if(check(TokenType.Uint))
        {
            if(sig==1)
                instructions.add(new Instruction(Operation.LIT, 0));

            instructions.add(new Instruction(Operation.LIT, (Integer)next().getValue()));
            if(sig==1)
            {
                instructions.add(new Instruction(Operation.SUB));
            }

        }
    }

    private void analyseExpression() throws CompileError {
        analyseItem();
        while(true)
        {
            if(check(TokenType.Plus))
            {
                next();
                analyseItem();
                instructions.add(new Instruction(Operation.ADD));
            }
            else if(check(TokenType.Minus))
            {
                next();
                analyseItem();
                instructions.add(new Instruction(Operation.SUB));
            }
            else
                break;
        }
    }

    private void analyseAssignmentStatement() throws CompileError {
        Token x=expect(TokenType.Ident);
        if(x!=null)
        {
            expect(TokenType.Equal);
            analyseExpression();
            int bias=getOffset(x.getValueString(),x.getStartPos());//看看是否定义过
            if(isConstant(x.getValueString(),x.getStartPos()))
                throw new AnalyzeError(ErrorCode.AssignToConstant, x.getStartPos());//常量不能再赋值了
            instructions.add(new Instruction(Operation.STO,bias));
            expect(TokenType.Semicolon);
        }
    }

    private void analyseOutputStatement() throws CompileError {
        expect(TokenType.Print);
        expect(TokenType.LParen);
        analyseExpression();
        expect(TokenType.RParen);
        expect(TokenType.Semicolon);
        instructions.add(new Instruction(Operation.WRT));
    }

    private void analyseItem() throws CompileError {
        analyseFactor();
        while(true)
        {
            if(check(TokenType.Div)||check(TokenType.Mult))
            {
                if(check(TokenType.Div))
                {
                    next();
                    analyseFactor();
                    instructions.add(new Instruction(Operation.DIV));
                }
                else{
                    next();
                    analyseFactor();
                    instructions.add(new Instruction(Operation.MUL));
                }
            }
            else break;
        }
    }

    private void analyseFactor() throws CompileError {
        boolean negate;
        if (nextIf(TokenType.Minus) != null) {
            negate = true;
            // 计算结果需要被 0 减
            instructions.add(new Instruction(Operation.LIT, 0));
        } else {
            nextIf(TokenType.Plus);
            negate = false;
        }

        if (check(TokenType.Ident)){
            // 调用相应的处理函数
            Token x=nextIf(TokenType.Ident);
            //查看是否定义过；
            int bias=getOffset(x.getValueString(),x.getStartPos());
            //查看是否付了值
            var entry = this.symbolTable.get(x.getValueString());
            if(entry.isInitialized)
            {
                instructions.add(new Instruction(Operation.LOD,bias));
            }
            else
            {
                throw new AnalyzeError(ErrorCode.NotInitialized, x.getStartPos());
            }

        } else if (check(TokenType.Uint)) {
            // 调用相应的处理函数
            Token x=nextIf(TokenType.Uint);

//            if(((Integer)x.getValue())>2147483647 || ((Integer)x.getValue())<0)
//                throw new AnalyzeError(ErrorCode.IntegerOverflow, x.getStartPos());
            instructions.add(new Instruction(Operation.LIT, (Integer)x.getValue()));

        } else if (check(TokenType.LParen)) {
            // 调用相应的处理函数
            nextIf(TokenType.LParen);

            instructions.add(new Instruction(Operation.LIT, 0));

            analyseExpression();

            expect(TokenType.RParen);
        } else {
            // 都不是，摸了
            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
        }

        if (negate) {
            instructions.add(new Instruction(Operation.SUB));
    }
    }
}
