import os

class GenerateAst:
    def __init__(self, relative_path, class_name, ast):
        self.path = os.path.abspath(relative_path)
        self.define_ast = ast
        self.class_name = class_name

    def create_file(self):
        file_path = os.path.join(self.path, f"{self.class_name}.java")

        with open(file_path, "w+") as f:
            self.write_package(f, "jlox")
            with self.CurlyBraceWrapper(f, self.generate_class_header(f, "abstract class", self.class_name)) as _:
                with self.CurlyBraceWrapper(f, self.generate_class_header(f, "interface", "Visitor<R>")) as _:
                    for class_type, fields in self.define_ast:
                        self.define_visitor(f, class_type)                
                for class_type, fields in self.define_ast:
                    with self.CurlyBraceWrapper(f, self.generate_class_header(f, "static class", class_type, self.class_name)) as _:
                        self.create_constructor(f, class_type, fields)
                        self.accept_override(f, class_type)
                        self.declare_fields(f, fields)
                self.define_abstract_accept(f)
                
    def accept_override(self, f, class_type):
        with self.CurlyBraceWrapper(f, "<R> R accept(Visitor<R> visitor)") as _:
            f.write(f"return visitor.visit{class_type}{self.class_name}(this);")
        
    def define_abstract_accept(self, f):
        f.write("abstract <R> R accept(Visitor<R> visitor);")
        
    def define_visitor(self, f, class_type):
        f.write(f" R visit{class_type}{self.class_name} ({class_type} {self.class_name.lower()});")
    
    def declare_fields(self, f, fields):
        for field_type, name in fields:
            f.write(f"final {field_type} {name};")
        
    def create_constructor(self, f, class_type, fields):
        with self.ParenthesisWrapper(f, prefix=class_type) as _:
            for i, field in enumerate(fields):
                field_type, name = field
                f.write(f"{field_type} {name}")
                if i != len(fields) - 1:
                    f.write(", ")

        with self.CurlyBraceWrapper(f) as _:
            for field in fields:
                field_type, name = field
                f.write(f"this.{name} = {name};")

    @staticmethod
    def write_package(f, package_name):
        f.write(f"package {package_name};")

    @staticmethod
    def generate_class_header(f, class_type, class_name, extends=None):
        def extensions():
            if extends:
                return f" extends {extends}"
            return None
        return (f"{class_type} {class_name}{extensions() or ' '}")

    class CurlyBraceWrapper:
        def __init__(self, file_writer, clause_header=""):
            self.file = file_writer
            self.clause_header = clause_header
        def __enter__(self):
            self.file.write(f"{self.clause_header}{'{'}")
        def __exit__(self, exc_type, exc_value, tb):
            self.file.write("}")

    class ParenthesisWrapper:
        def __init__(self, file_writer, prefix = '', postfix = ''):
            self.file = file_writer
            self.prefix = prefix
            self.postfix = postfix
        def __enter__(self):
            self.file.write(f"{self.prefix}(")
        def __exit__(self, exc_type, exc_value, tb):
            self.file.write(f"){self.postfix}")

if __name__ == "__main__":
    expression_ast_props = [
            ("Assign", [("Token", "name"), ("jlox.Expression", "value")]),
            ("Binary", [("Expression", "left"), ("Token", "operator"), ("Expression", "right")]),
            ("Grouping", [("Expression", "expression")]),
            ("Literal", [("Object", "value")]),
            ("Unary", [("Token", "operator"), ("Expression", "right")]),
            ("Variable", [("Token", "name")]),
        ]
    genAst = GenerateAst("jlox", "Expression", expression_ast_props)
    genAst.create_file()
    
    statement_ast_props = [
        ("Expression", [("jlox.Expression", "expression")]),
        ("Print", [("jlox.Expression", "expression")]),
        ("Var", [("Token", "name"), ("jlox.Expression", "initializer")]),
        
    ]
    genAst = GenerateAst("jlox", "Statement", statement_ast_props)
    genAst.create_file()

