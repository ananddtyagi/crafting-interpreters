import os


class GenerateAst:
    def __init__(self):
        # take in relative from input
        self.relative_path = "jlox"
        self.path = os.path.abspath(self.relative_path)
        self.defineAst = [
            (
                "Binary",
                [
                    ("Expression", "left"),
                    ("Token", "operator"),
                    ("Expression", "right"),
                ],
            ),
            ("Grouping", [("Expression", "expression")]),
            ("Literal", [("Object", "value")]),
            ("Unary", [("Token", "operator"), ("Expression", "right")]),
        ]

    def create_expression_file(self):
        file_path = os.path.join(self.path, "Expression.java")

        with open(file_path, "w+") as f:
            self.curlyBraceWrapper = self.CurlyBraceWrapper(f)
            self.parenthesisWrapper = self.ParenthesisWrapper(f)
            self.write_package(f, "jlox")
            self.class_header(f, "abstract", "Expression")
            with self.curlyBraceWrapper as _:
                for class_type, fields in self.defineAst:
                    self.class_header(f, "static", class_type, "Expression")
                    with self.curlyBraceWrapper as _:
                        self.create_constructor(f, class_type, fields)
                        self.declare_fields(f, fields)
                    
    def declare_fields(self, f, fields):
        for field_type, name in fields:
            f.write(f"final {field_type} {name};")
        
    def create_constructor(self, f, class_type, fields):
        f.write(f"{class_type}")
        with self.parenthesisWrapper as _:
            for i, field in enumerate(fields):
                field_type, name = field
                f.write(f"{field_type} {name}")
                if i != len(fields) - 1:
                    f.write(", ")

        with self.curlyBraceWrapper as _:
            for field in fields:
                field_type, name = field
                f.write(f"this.{name} = {name};")

    @staticmethod
    def write_package(f, package_name):
        f.write(f"package {package_name};")

    @staticmethod
    def class_header(f, class_type, class_name, *extends):
        def extensions():
            if extends:
                return f" extends {extends[0]}"
            return None

        f.write(f"{class_type} class {class_name}{extensions() or ' '}")

    class CurlyBraceWrapper:
        def __init__(self, file_writer):
            self.file = file_writer

        def __enter__(self):
            self.file.write("{")

        def __exit__(self, exc_type, exc_value, tb):
            self.file.write("}")

    class ParenthesisWrapper:
        def __init__(self, file_writer):
            self.file = file_writer

        def __enter__(self):
            self.file.write("(")

        def __exit__(self, exc_type, exc_value, tb):
            self.file.write(")")


if __name__ == "__main__":
    genAst = GenerateAst()
    genAst.create_expression_file()
